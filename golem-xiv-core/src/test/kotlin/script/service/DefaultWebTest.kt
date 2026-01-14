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

import com.xemantic.ai.golem.api.backend.script.WebBrowser
import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultWebTest {

    @Test
    fun `should perform DDGS search with default parameters`() = runTest {
        // given
        val mockResults = listOf(
            mapOf(
                "title" to "Kotlin Programming Language",
                "href" to "https://kotlinlang.org",
                "body" to "Kotlin is a modern programming language that makes developers happier."
            ),
            mapOf(
                "title" to "Kotlin Documentation",
                "href" to "https://kotlinlang.org/docs",
                "body" to "Official Kotlin documentation and tutorials."
            )
        )

        val mockEngine = MockEngine { request ->
            assert(request.url.toString().startsWith("http://localhost:8001/search"))
            assert(request.url.parameters["query"] == "kotlin programming")
            assert(request.url.parameters["page"] == "1")
            assert(request.url.parameters["max_results"] == "10")
            assert(request.url.parameters["region"] == "us-en")
            assert(request.url.parameters["safesearch"] == "moderate")
            assert(request.url.parameters["backend"] == "auto")

            respond(
                content = Json.encodeToString(mockResults),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val web = DefaultWeb(httpClient)

        // when
        val result = web.search("kotlin programming")

        // then
        assertContains(result, "## Search Results")
        assertContains(result, "Kotlin Programming Language")
        assertContains(result, "https://kotlinlang.org")
        assertContains(result, "Kotlin is a modern programming language")
        assertContains(result, "Kotlin Documentation")
    }

    @Test
    fun `should perform DDGS search with custom parameters`() = runTest {
        // given
        val mockResults = listOf(
            mapOf(
                "title" to "Recent Kotlin News",
                "href" to "https://blog.jetbrains.com/kotlin",
                "body" to "Latest updates about Kotlin"
            )
        )

        val mockEngine = MockEngine { request ->
            assert(request.url.parameters["query"] == "kotlin news")
            assert(request.url.parameters["page"] == "2")
            assert(request.url.parameters["max_results"] == "5")
            assert(request.url.parameters["region"] == "us-en")
            assert(request.url.parameters["safesearch"] == "off")
            assert(request.url.parameters["timelimit"] == "w")

            respond(
                content = Json.encodeToString(mockResults),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val web = DefaultWeb(httpClient)

        // when
        val result = web.search(
            query = "kotlin news",
            page = 2,
            pageSize = 5,
            region = "us-en",
            safesearch = "off",
            timelimit = "w"
        )

        // then
        assertContains(result, "Recent Kotlin News")
        assertContains(result, "https://blog.jetbrains.com/kotlin")
    }

    @Test
    fun `should return no results message when DDGS returns empty list`() = runTest {
        // given
        val mockEngine = MockEngine { request ->
            respond(
                content = Json.encodeToString(emptyList<Map<String, String>>()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val web = DefaultWeb(httpClient)

        // when
        val result = web.search("nonexistent query xyz123")

        // then
        assert(result == "No search results found.")
    }

    @Test
    fun `should handle DDGS service unavailable`() = runTest {
        // given
        val mockEngine = MockEngine { request ->
            respond(
                content = "",
                status = HttpStatusCode.ServiceUnavailable
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val web = DefaultWeb(httpClient)

        // when/then
        val exception = assertFailsWith<IllegalStateException> {
            web.search("test query")
        }
        assertContains(exception.message ?: "", "DDGS search service is not available")
    }

    @Test
    fun `should throw exception for anthropic provider`() = runTest {
        // given
        val mockEngine = MockEngine { request ->
            respond(content = "[]", status = HttpStatusCode.OK)
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val web = DefaultWeb(httpClient)

        // when/then
        val exception = assertFailsWith<UnsupportedOperationException> {
            web.search("test query", provider = "anthropic")
        }
        assertContains(exception.message ?: "", "Anthropic WebSearch requires integration")
    }

    @Test
    fun `should throw exception for unknown provider`() = runTest {
        // given
        val mockEngine = MockEngine { request ->
            respond(content = "[]", status = HttpStatusCode.OK)
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val web = DefaultWeb(httpClient)

        // when/then
        val exception = assertFailsWith<IllegalArgumentException> {
            web.search("test query", provider = "unknown")
        }
        assertContains(exception.message ?: "", "Unknown search provider: unknown")
    }

    @Test
    fun `should open URL using jina fallback when no WebBrowser provided`() = runTest {
        // given
        val mockEngine = MockEngine { request ->
            assert(request.url.toString().startsWith("https://r.jina.ai/"))
            respond(
                content = "# Kotlin Programming\n\nKotlin is awesome!",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/markdown")
            )
        }

        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(httpClient, webBrowser = null)

        // when
        val result = web.open("https://kotlinlang.org")

        // then
        assertContains(result, "Kotlin Programming")
        assertContains(result, "Kotlin is awesome!")
    }

    @Test
    fun `should handle jina fallback failure`() = runTest {
        // given
        val mockEngine = MockEngine { request ->
            respond(
                content = "Error",
                status = HttpStatusCode.BadGateway
            )
        }

        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(httpClient, webBrowser = null)

        // when/then
        val exception = assertFailsWith<IllegalStateException> {
            web.open("https://example.com")
        }
        assertContains(exception.message ?: "", "Failed to fetch")
        assertContains(exception.message ?: "", "via jina.ai")
    }

    @Test
    fun `should format DDGS results with multiple entries correctly`() = runTest {
        // given
        val mockResults = listOf(
            mapOf(
                "title" to "Result 1",
                "href" to "https://example1.com",
                "body" to "Description 1"
            ),
            mapOf(
                "title" to "Result 2",
                "href" to "https://example2.com",
                "body" to "Description 2"
            ),
            mapOf(
                "title" to "Result 3",
                "href" to "https://example3.com",
                "body" to "Description 3"
            )
        )

        val mockEngine = MockEngine { request ->
            respond(
                content = Json.encodeToString(mockResults),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val web = DefaultWeb(httpClient)

        // when
        val result = web.search("test")

        // then
        assertContains(result, "1. **Result 1** — https://example1.com")
        assertContains(result, "   Description 1")
        assertContains(result, "2. **Result 2** — https://example2.com")
        assertContains(result, "   Description 2")
        assertContains(result, "3. **Result 3** — https://example3.com")
        assertContains(result, "   Description 3")
    }

    // Session-based tests

    @Test
    fun `should delegate openInSession to WebBrowser`() = runTest {
        // given
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

        val web = DefaultWeb(httpClient, webBrowser = mockWebBrowser)

        // when
        val result = web.openInSession("test-session", "https://example.com")

        // then
        assertEquals("test-session", capturedSessionId)
        assertEquals("https://example.com", capturedUrl)
        assertContains(result, "Session Content")
    }

    @Test
    fun `should throw UnsupportedOperationException for openInSession when no WebBrowser`() = runTest {
        // given
        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(httpClient, webBrowser = null)

        // when/then
        val exception = assertFailsWith<UnsupportedOperationException> {
            web.openInSession("test-session", "https://example.com")
        }
        assertContains(exception.message ?: "", "Session-based browsing requires Playwright")
    }

    @Test
    fun `should delegate closeSession to WebBrowser`() = runTest {
        // given
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

        val web = DefaultWeb(httpClient, webBrowser = mockWebBrowser)

        // when
        web.closeSession("my-session")

        // then
        assertEquals("my-session", closedSessionId)
    }

    @Test
    fun `should handle closeSession gracefully when no WebBrowser`() = runTest {
        // given
        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(httpClient, webBrowser = null)

        // when/then - should not throw
        web.closeSession("non-existent")
    }

    @Test
    fun `should delegate listSessions to WebBrowser`() = runTest {
        // given
        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)

        val mockWebBrowser = object : WebBrowser {
            override suspend fun open(url: String) = "content"
            override suspend fun openInSession(sessionId: String, url: String) = "content"
            override suspend fun closeSession(sessionId: String) {}
            override fun listSessions() = setOf("session-1", "session-2")
        }

        val web = DefaultWeb(httpClient, webBrowser = mockWebBrowser)

        // when
        val sessions = web.listSessions()

        // then
        assertEquals(setOf("session-1", "session-2"), sessions)
    }

    @Test
    fun `should return empty set for listSessions when no WebBrowser`() = runTest {
        // given
        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val httpClient = HttpClient(mockEngine)
        val web = DefaultWeb(httpClient, webBrowser = null)

        // when
        val sessions = web.listSessions()

        // then
        assert(sessions.isEmpty())
    }

    @Test
    fun `should not fallback to jina for openInSession failures`() = runTest {
        // given
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

        val web = DefaultWeb(httpClient, webBrowser = mockWebBrowser)

        // when/then - should propagate the exception, not fallback
        val exception = assertFailsWith<RuntimeException> {
            web.openInSession("test", "https://example.com")
        }
        assertContains(exception.message ?: "", "Browser navigation failed")
    }

}
