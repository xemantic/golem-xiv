/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.golem.core.script.service

import com.xemantic.ai.golem.api.backend.SearchProvider
import com.xemantic.ai.golem.api.backend.script.WebBrowser
import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class DefaultWebTest {

    // Mock SearchProvider for tests
    private class MockSearchProvider(
        private val results: String = "Mock search results"
    ) : SearchProvider {
        var lastQuery: String? = null
        var lastPage: Int? = null
        var lastPageSize: Int? = null
        var lastRegion: String? = null
        var lastSafeSearch: String? = null
        var lastTimeLimit: String? = null

        override suspend fun search(
            query: String,
            page: Int,
            pageSize: Int,
            region: String,
            safeSearch: String,
            timeLimit: String?
        ): String {
            lastQuery = query
            lastPage = page
            lastPageSize = pageSize
            lastRegion = region
            lastSafeSearch = safeSearch
            lastTimeLimit = timeLimit
            return results
        }
    }

    @Test
    fun `should delegate search to provider with default parameters`() = runTest {
        // given
        val mockProvider = MockSearchProvider("## Search Results\n\n1. **Test Result**")
        val searchProviders = mapOf<String?, SearchProvider>(
            null to mockProvider,
            "ddgs" to mockProvider
        )

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(searchProviders, httpClient)

        // when
        val result = web.search("kotlin programming")

        // then
        mockProvider should {
            have(lastQuery == "kotlin programming")
            have(lastPage == 1)
            have(lastPageSize == 10)
            have(lastRegion == "us-en")
            have(lastSafeSearch == "moderate")
            have(lastTimeLimit == null)
        }
        assert("Search Results" in result)
    }

    @Test
    fun `should delegate search to provider with custom parameters`() = runTest {
        // given
        val mockProvider = MockSearchProvider("## Search Results\n\n1. **Recent Kotlin News**")
        val searchProviders = mapOf<String?, SearchProvider>(
            null to mockProvider,
            "ddgs" to mockProvider
        )

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(searchProviders, httpClient)

        // when
        val result = web.search(
            query = "kotlin news",
            page = 2,
            pageSize = 5,
            region = "uk-en",
            safeSearch = "off",
            timeLimit = "w"
        )

        // then
        mockProvider should {
            have(lastQuery == "kotlin news")
            have(lastPage == 2)
            have(lastPageSize == 5)
            have(lastRegion == "uk-en")
            have(lastSafeSearch == "off")
            have(lastTimeLimit == "w")
        }
        assert("Recent Kotlin News" in result)
    }

    @Test
    fun `should delegate to named provider`() = runTest {
        // given
        val defaultProvider = MockSearchProvider("Default results")
        val namedProvider = MockSearchProvider("Named results")
        val searchProviders = mapOf<String?, SearchProvider>(
            null to defaultProvider,
            "ddgs" to namedProvider
        )

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(searchProviders, httpClient)

        // when
        val result = web.search("test query", provider = "ddgs")

        // then
        assert(namedProvider.lastQuery == "test query")
        assert(defaultProvider.lastQuery == null)
        assert("Named results" in result)
    }

    @Test
    fun `should throw exception for unknown provider`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(
            null to mockProvider,
            "ddgs" to mockProvider
        )

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(searchProviders, httpClient)

        // when/then
        val exception = assertFailsWith<IllegalArgumentException> {
            web.search("test query", provider = "unknown")
        }
        assert("Unknown search provider: unknown" in (exception.message ?: ""))
    }

    @Test
    fun `should fetch URL using jina fallback when no WebBrowser provided`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(null to mockProvider)

        val mockEngine = MockEngine { request ->
            assert(request.url.toString().startsWith("https://r.jina.ai/"))
            respond(
                content = "# Kotlin Programming\n\nKotlin is awesome!",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/markdown")
            )
        }

        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(searchProviders, httpClient, webBrowser = null)

        // when
        val result = web.fetch("https://kotlinlang.org")

        // then
        assert("Kotlin Programming" in result)
        assert("Kotlin is awesome!" in result)
    }

    @Test
    fun `should handle jina fallback failure`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(null to mockProvider)

        val mockEngine = MockEngine { request ->
            respond(
                content = "Error",
                status = HttpStatusCode.BadGateway
            )
        }

        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(searchProviders, httpClient, webBrowser = null)

        // when/then
        val exception = assertFailsWith<IllegalStateException> {
            web.fetch("https://example.com")
        }
        exception should {
            have("Failed to fetch" in (message ?: ""))
            have("via jina.ai" in (message ?: ""))
        }
    }

    // Session-based tests

    @Test
    fun `should delegate openInSession to WebBrowser`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(null to mockProvider)

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)

        var capturedSessionId: String? = null
        var capturedUrl: String? = null

        val mockWebBrowser = object : WebBrowser {
            override suspend fun open(url: String) = "stateless content"
            override suspend fun openInSession(sessionId: String, url: String): String {
                capturedSessionId = sessionId
                capturedUrl = url
                return "# Session Content\n\nThis is session content."
            }
            override suspend fun closeSession(sessionId: String) {}
            override fun listSessions() = emptySet<String>()
        }

        val web = DefaultWeb(searchProviders, httpClient, webBrowser = mockWebBrowser)

        // when
        val result = web.openInSession("test-session", "https://example.com")

        // then
        assert(capturedSessionId == "test-session")
        assert(capturedUrl == "https://example.com")
        assert("Session Content" in result)
    }

    @Test
    fun `should throw UnsupportedOperationException for openInSession when no WebBrowser`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(null to mockProvider)

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(searchProviders, httpClient, webBrowser = null)

        // when/then
        val exception = assertFailsWith<UnsupportedOperationException> {
            web.openInSession("test-session", "https://example.com")
        }
        assert("Session-based browsing requires Playwright" in (exception.message ?: ""))
    }

    @Test
    fun `should delegate closeSession to WebBrowser`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(null to mockProvider)

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)

        var closedSessionId: String? = null

        val mockWebBrowser = object : WebBrowser {
            override suspend fun open(url: String) = "content"
            override suspend fun openInSession(sessionId: String, url: String) = "content"
            override suspend fun closeSession(sessionId: String) {
                closedSessionId = sessionId
            }
            override fun listSessions() = emptySet<String>()
        }

        val web = DefaultWeb(searchProviders, httpClient, webBrowser = mockWebBrowser)

        // when
        web.closeSession("my-session")

        // then
        assert(closedSessionId == "my-session")
    }

    @Test
    fun `should handle closeSession gracefully when no WebBrowser`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(null to mockProvider)

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(searchProviders, httpClient, webBrowser = null)

        // when/then - should not throw
        web.closeSession("non-existent")
    }

    @Test
    fun `should delegate listSessions to WebBrowser`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(null to mockProvider)

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)

        val mockWebBrowser = object : WebBrowser {
            override suspend fun open(url: String) = "content"
            override suspend fun openInSession(sessionId: String, url: String) = "content"
            override suspend fun closeSession(sessionId: String) {}
            override fun listSessions() = setOf("session-1", "session-2")
        }

        val web = DefaultWeb(searchProviders, httpClient, webBrowser = mockWebBrowser)

        // when
        val sessions = web.listSessions()

        // then
        assert(sessions == setOf("session-1", "session-2"))
    }

    @Test
    fun `should return empty set for listSessions when no WebBrowser`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(null to mockProvider)

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(searchProviders, httpClient, webBrowser = null)

        // when
        val sessions = web.listSessions()

        // then
        assert(sessions.isEmpty())
    }

    @Test
    fun `should not fallback to jina for openInSession failures`() = runTest {
        // given
        val mockProvider = MockSearchProvider()
        val searchProviders = mapOf<String?, SearchProvider>(null to mockProvider)

        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)

        val mockWebBrowser = object : WebBrowser {
            override suspend fun open(url: String) = "content"
            override suspend fun openInSession(sessionId: String, url: String): String {
                throw RuntimeException("Browser navigation failed")
            }
            override suspend fun closeSession(sessionId: String) {}
            override fun listSessions() = emptySet<String>()
        }

        val web = DefaultWeb(searchProviders, httpClient, webBrowser = mockWebBrowser)

        // when/then - should propagate the exception, not fallback
        val exception = assertFailsWith<RuntimeException> {
            web.openInSession("test", "https://example.com")
        }
        assert("Browser navigation failed" in (exception.message ?: ""))
    }

}
