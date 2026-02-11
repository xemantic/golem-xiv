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
import com.xemantic.ai.golem.api.backend.script.MarkdownContentType
import com.xemantic.ai.golem.api.backend.script.Web
import com.xemantic.ai.golem.api.backend.script.WebBrowser
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess

private val logger = KotlinLogging.logger {}

private const val DEFAULT_HTTP_TIMEOUT_MS = 30_000L

class DefaultWeb(
    private val searchProviders: Map<String?, SearchProvider>,
    private val httpClient: HttpClient,
    private val webBrowser: WebBrowser? = null,
    private val httpTimeoutMs: Long = DEFAULT_HTTP_TIMEOUT_MS
) : Web {

    override suspend fun fetch(url: String, accept: ContentType): String {
        return if (webBrowser != null) {
            try {
                logger.debug { "Fetching URL with Playwright: $url" }
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
                fetchWithJina(url)
            }
        } else {
            logger.debug { "No Playwright browser available, using jina.ai for: $url" }
            fetchWithJina(url)
        }
    }

    // Session methods kept for future implementation but not part of public interface
    suspend fun openInSession(sessionId: String, url: String): String {
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

    suspend fun closeSession(sessionId: String) {
        if (webBrowser == null) {
            logger.warn { "closeSession('$sessionId') called but no browser available" }
            return
        }
        logger.debug { "Closing session: $sessionId" }
        webBrowser.closeSession(sessionId)
    }

    fun listSessions(): Set<String> {
        return webBrowser?.listSessions() ?: emptySet()
    }

    private suspend fun fetchWithJina(url: String): String {
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
        safeSearch: String,
        timeLimit: String?
    ): String {
        val searchProvider = searchProviders[provider]
            ?: throw IllegalArgumentException(
                "Unknown search provider: $provider. " +
                "Available providers: ${searchProviders.keys.filterNotNull().joinToString(", ")}"
            )
        return searchProvider.search(query, page, pageSize, region, safeSearch, timeLimit)
    }

}
