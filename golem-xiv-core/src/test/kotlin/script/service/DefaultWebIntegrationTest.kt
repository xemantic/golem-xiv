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
import com.xemantic.ai.golem.ddgs.DdgsSearchProvider
import com.xemantic.kotlin.test.assert
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import kotlin.test.Test

/**
 * Integration tests for DefaultWeb that make actual calls to the DDGS service.
 *
 * These tests require the DDGS service to be running:
 * ```
 * ./gradlew runDdgsSearch
 * ```
 *
 * Run these tests with:
 * ```
 * # Run all integration tests
 * ./gradlew :golem-xiv-core:test --tests "*Integration*"
 *
 * # Or by tag
 * ./gradlew :golem-xiv-core:test --tests "*" -Dkotest.tags="integration"
 * ```
 *
 * Tests will be skipped (not failed) if the DDGS service is not available.
 */
@Tag("integration")
class DefaultWebIntegrationTest {

    companion object {
        private var ddgsServiceAvailable = false
        private const val DDGS_SERVICE_URL = "http://localhost:8001"

        @JvmStatic
        @BeforeAll
        fun checkServiceAvailability() = runTest {
            try {
                val client = HttpClient(Java)
                val response = client.get("$DDGS_SERVICE_URL/health")
                ddgsServiceAvailable = response.status.isSuccess()
                client.close()
                println("DDGS service is ${if (ddgsServiceAvailable) "available" else "not available"}")
            } catch (e: Exception) {
                ddgsServiceAvailable = false
                println("DDGS service is not available: ${e.message}")
            }
        }
    }

    private fun assumeDdgsServiceAvailable() {
        assumeTrue(
            ddgsServiceAvailable,
            "DDGS service is not running. Start it with: ./gradlew runDdgsSearch"
        )
    }

    private fun createWeb(httpClient: HttpClient): DefaultWeb {
        val ddgsProvider = DdgsSearchProvider(httpClient, DDGS_SERVICE_URL)
        val searchProviders = mapOf<String?, SearchProvider>(
            null to ddgsProvider,
            "ddgs" to ddgsProvider
        )
        return DefaultWeb(searchProviders, httpClient)
    }

    @Test
    fun `should perform actual search query using DDGS`() = runTest {
        assumeDdgsServiceAvailable()

        // given
        val httpClient = HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
        }
        val web = createWeb(httpClient)

        // when
        val result = web.search("Kotlin programming language")

        // then
        assert("## Search Results" in result)
        // Verify we got some results (not empty)
        assert(result.lines().size > 5)
        assert("**" in result && "—" in result)

        httpClient.close()
    }

    @Test
    fun `should handle search with custom parameters`() = runTest {
        assumeDdgsServiceAvailable()

        // given
        val httpClient = HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
        }
        val web = createWeb(httpClient)

        // when
        val result = web.search(
            query = "Kotlin",
            page = 1,
            pageSize = 5,
            region = "us-en",
            safeSearch = "moderate"
        )

        // then
        assert("## Search Results" in result)
        assert(result.isNotEmpty())

        httpClient.close()
    }

    @Test
    fun `should handle search with time filter`() = runTest {
        assumeDdgsServiceAvailable()

        // given
        val httpClient = HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
        }
        val web = createWeb(httpClient)

        // when - search for news from past week
        val result = web.search(
            query = "technology news",
            timeLimit = "w"  // past week
        )

        // then
        assert("## Search Results" in result)
        // Should get recent results
        assert("http" in result || "https" in result)

        httpClient.close()
    }

    @Test
    fun `should handle unusual query gracefully`() = runTest {
        assumeDdgsServiceAvailable()

        // given
        val httpClient = HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
        }
        val web = createWeb(httpClient)

        // when - search for extremely unlikely to exist query
        val result = web.search("xyzabc123impossible987654321query")

        // then - should return valid markdown format regardless of results
        assert("## Search Results" in result)
        // Even nonsense queries might return fuzzy match results, which is fine
        // We just verify the service responds and formats correctly
        assert(result.isNotEmpty())

        httpClient.close()
    }

    @Test
    fun `should handle pagination correctly`() = runTest {
        assumeDdgsServiceAvailable()

        // given
        val httpClient = HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
        }
        val web = createWeb(httpClient)

        // when - get first and second page
        val page1 = web.search(query = "programming", page = 1, pageSize = 3)
        val page2 = web.search(query = "programming", page = 2, pageSize = 3)

        // then - both should have results and be different
        assert("## Search Results" in page1)
        assert("## Search Results" in page2)

        // Extract URLs from both pages to verify they're different
        val urlPattern = Regex("https?://[^\\s\\)]+")
        val urls1 = urlPattern.findAll(page1).map { it.value }.toSet()
        val urls2 = urlPattern.findAll(page2).map { it.value }.toSet()

        // Pages should have different content (different URLs)
        assert(urls1.intersect(urls2).size < urls1.size)

        httpClient.close()
    }

    @Test
    fun `should handle different regions correctly`() = runTest {
        assumeDdgsServiceAvailable()

        // given
        val httpClient = HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
        }
        val web = createWeb(httpClient)

        // when - search in US region
        val result = web.search(
            query = "news",
            region = "us-en"
        )

        // then - should get results
        assert("## Search Results" in result)
        assert("http" in result)

        httpClient.close()
    }

    @Test
    fun `should verify search result format`() = runTest {
        assumeDdgsServiceAvailable()

        // given
        val httpClient = HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
        }
        val web = createWeb(httpClient)

        // when
        val result = web.search("open source")

        // then - verify markdown format
        assert("## Search Results" in result)

        // Should have numbered items with bold titles
        val lines = result.lines()
        val numberedItems = lines.filter { it.trim().matches(Regex("^\\d+\\.\\s+\\*\\*.*")) }
        assert(numberedItems.isNotEmpty())

        // Should have URLs
        assert("http://" in result || "https://" in result)

        // Should have the em dash separator between title and URL
        assert("—" in result)

        httpClient.close()
    }

    @Test
    fun `should handle special characters in query`() = runTest {
        assumeDdgsServiceAvailable()

        // given
        val httpClient = HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
        }
        val web = createWeb(httpClient)

        // when - query with special characters
        val result = web.search("\"Kotlin\" AND \"JVM\"")

        // then - should handle query and return results
        assert("## Search Results" in result)

        httpClient.close()
    }

    @Test
    fun `should use jina fallback when WebBrowser is null`() = runTest {
        // This test doesn't require DDGS, it tests the jina.ai fallback

        // given
        val httpClient = HttpClient(Java)
        val ddgsProvider = DdgsSearchProvider(httpClient)
        val searchProviders = mapOf<String?, SearchProvider>(null to ddgsProvider)
        val web = DefaultWeb(searchProviders, httpClient, webBrowser = null)

        // when - try to open a simple website
        val result = web.fetch("https://example.com")

        // then - should get markdown content
        assert(result.isNotEmpty())
        // example.com typically has "Example Domain" in title
        assert("Example" in result || "domain" in result)

        httpClient.close()
    }

}
