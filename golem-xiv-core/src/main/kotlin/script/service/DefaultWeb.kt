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

import com.xemantic.ai.golem.api.backend.script.Web
import com.xemantic.ai.golem.api.backend.script.WebBrowser
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import java.net.ConnectException

private val logger = KotlinLogging.logger {}

private const val DEFAULT_DDGS_SERVICE_URL = "http://localhost:8001"
private const val DDGS_SERVICE_URL_ENV = "GOLEM_DDGS_SERVICE_URL"
private const val DEFAULT_HTTP_TIMEOUT_MS = 30_000L

/**
 * Default implementation of the Web service supporting multiple providers.
 *
 * For opening URLs:
 * - Uses Playwright (via WebBrowser) if available for JavaScript rendering
 * - Falls back to jina.ai for simple HTML to Markdown conversion
 *
 * For search:
 * - Uses DDGS service (free, local) as default
 * - Supports Anthropic WebSearch as premium option (requires separate integration)
 *
 * @param httpClient HTTP client for making requests
 * @param webBrowser Optional Playwright browser for JavaScript rendering
 * @param ddgsServiceUrl URL of the DDGS search service. Defaults to environment variable
 *                       GOLEM_DDGS_SERVICE_URL, or http://localhost:8001 if not set.
 * @param httpTimeoutMs Timeout for HTTP requests in milliseconds (default: 30000)
 */
class DefaultWeb(
    private val httpClient: HttpClient,
    private val webBrowser: WebBrowser? = null,
    private val ddgsServiceUrl: String = System.getenv(DDGS_SERVICE_URL_ENV) ?: DEFAULT_DDGS_SERVICE_URL,
    private val httpTimeoutMs: Long = DEFAULT_HTTP_TIMEOUT_MS
) : Web {

    override suspend fun open(url: String): String {
        return if (webBrowser != null) {
            try {
                logger.debug { "Opening URL with Playwright: $url" }
                val result = webBrowser.open(url)
                logger.debug {
                    buildString {
                        appendLine("Playwright result for URL: $url")
                        appendLine("Content length: ${result.length} characters")
                        appendLine("First 500 characters:")
                        appendLine(result.take(500))
                        if (result.length > 500) {
                            appendLine("...")
                            appendLine("Last 200 characters:")
                            appendLine(result.takeLast(200))
                        }
                    }
                }
                result
            } catch (e: Exception) {
                // Playwright failed, fallback to jina.ai
                logger.debug { "Playwright failed for $url, falling back to jina.ai: ${e.message}" }
                openWithJina(url)
            }
        } else {
            logger.debug { "No Playwright browser available, using jina.ai for: $url" }
            openWithJina(url)
        }
    }

    override suspend fun openInSession(sessionId: String, url: String): String {
        if (webBrowser == null) {
            throw UnsupportedOperationException(
                "Session-based browsing requires Playwright. " +
                "Ensure Playwright is properly initialized."
            )
        }
        logger.debug { "Opening URL in session '$sessionId': $url" }
        return try {
            webBrowser.openInSession(sessionId, url)
        } catch (e: Exception) {
            logger.warn(e) { "Session '$sessionId' navigation failed for $url" }
            throw e  // Don't fallback - sessions require browser
        }
    }

    override suspend fun closeSession(sessionId: String) {
        if (webBrowser == null) {
            logger.warn { "closeSession('$sessionId') called but no browser available" }
            return
        }
        logger.debug { "Closing session: $sessionId" }
        webBrowser.closeSession(sessionId)
    }

    override fun listSessions(): Set<String> {
        return webBrowser?.listSessions() ?: emptySet()
    }

    private suspend fun openWithJina(url: String): String {
        val jinaUrl = "https://r.jina.ai/$url"
        logger.debug { "Fetching via jina.ai: $url" }
        val response = httpClient.get(jinaUrl) {
            timeout {
                requestTimeoutMillis = httpTimeoutMs
            }
        }
        return if (response.status.isSuccess()) {
            val result = response.bodyAsText()
            logger.debug {
                buildString {
                    appendLine("Jina.ai result for URL: $url")
                    appendLine("Content length: ${result.length} characters")
                    appendLine("First 500 characters:")
                    appendLine(result.take(500))
                    if (result.length > 500) {
                        appendLine("...")
                        appendLine("Last 200 characters:")
                        appendLine(result.takeLast(200))
                    }
                }
            }
            result
        } else {
            throw IllegalStateException("Failed to fetch $url via jina.ai: ${response.status}")
        }
    }

    override suspend fun search(
        query: String,
        provider: String?,
        page: Int,
        pageSize: Int,
        region: String,
        safesearch: String,
        timelimit: String?
    ): String {
        return when (provider) {
            "anthropic" -> {
                throw UnsupportedOperationException(
                    "Anthropic WebSearch requires integration at the server level. " +
                    "Use provider='ddgs' or null for local search."
                )
            }
            "ddgs", null -> searchWithDdgs(query, page, pageSize, region, safesearch, timelimit)
            else -> throw IllegalArgumentException("Unknown search provider: $provider. Use 'ddgs' or 'anthropic'.")
        }
    }

    private suspend fun searchWithDdgs(
        query: String,
        page: Int,
        pageSize: Int,
        region: String,
        safesearch: String,
        timelimit: String?
    ): String {
        try {
            val response = httpClient.get("$ddgsServiceUrl/search") {
                timeout {
                    requestTimeoutMillis = httpTimeoutMs
                }
                parameter("query", query)
                parameter("page", page)
                parameter("max_results", pageSize)
                parameter("region", region)
                parameter("safesearch", safesearch)
                timelimit?.let { parameter("timelimit", it) }
                parameter("backend", "auto")
            }

            if (!response.status.isSuccess()) {
                throw IllegalStateException("DDGS service returned error: ${response.status}")
            }

            val results: List<DdgsSearchResult> = response.body()

            logger.debug {
                buildString {
                    appendLine("DDGS search results for query: '$query'")
                    appendLine("Parameters: page=$page, pageSize=$pageSize, region=$region, safesearch=$safesearch, timelimit=$timelimit")
                    appendLine("Results count: ${results.size}")
                    appendLine("Raw results:")
                    results.forEachIndexed { index, result ->
                        appendLine("  [$index] Title: ${result.title}")
                        appendLine("      URL: ${result.href}")
                        appendLine("      Body: ${result.body}")
                        if (index < results.size - 1) appendLine()
                    }
                }
            }

            return formatDdgsResults(results)

        } catch (e: ConnectException) {
            throw IllegalStateException(
                "DDGS search service is not running. " +
                "Start it with: ./gradlew runDdgsSearch",
                e
            )
        } catch (e: HttpRequestTimeoutException) {
            throw IllegalStateException(
                "DDGS search request timed out after ${httpTimeoutMs}ms for query: '$query'",
                e
            )
        } catch (e: SerializationException) {
            throw IllegalStateException(
                "DDGS search response format mismatch - API contract may have changed. " +
                "Expected fields: title, href, body",
                e
            )
        }
    }

    private fun formatDdgsResults(results: List<DdgsSearchResult>): String {
        if (results.isEmpty()) {
            return "No search results found."
        }

        return buildString {
            appendLine("## Search Results")
            appendLine()
            results.forEachIndexed { index, result ->
                appendLine("${index + 1}. **${result.title}** — ${result.href}")
                appendLine("   ${result.body}")
                appendLine()
            }
        }
    }

}

@Serializable
internal data class DdgsSearchResult(
    val title: String,
    val href: String,
    val body: String
)
