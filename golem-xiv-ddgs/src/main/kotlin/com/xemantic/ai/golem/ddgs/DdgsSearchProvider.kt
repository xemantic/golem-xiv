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

package com.xemantic.ai.golem.ddgs

import com.xemantic.ai.golem.api.backend.SearchProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import java.net.ConnectException

private val logger = KotlinLogging.logger {}

private const val DEFAULT_DDGS_SERVICE_URL = "http://localhost:8001"
private const val DDGS_SERVICE_URL_ENV = "GOLEM_DDGS_SERVICE_URL"
private const val DEFAULT_HTTP_TIMEOUT_MS = 30_000L

class DdgsSearchProvider(
    private val httpClient: HttpClient,
    private val ddgsServiceUrl: String = System.getenv(DDGS_SERVICE_URL_ENV) ?: DEFAULT_DDGS_SERVICE_URL,
    private val httpTimeoutMs: Long = DEFAULT_HTTP_TIMEOUT_MS
) : SearchProvider {

    override suspend fun search(
        query: String,
        page: Int,
        pageSize: Int,
        region: String,
        safeSearch: String,
        timeLimit: String?
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
                parameter("safesearch", safeSearch)
                timeLimit?.let { parameter("timelimit", it) }
                parameter("backend", "auto")
            }

            if (!response.status.isSuccess()) {
                throw IllegalStateException("DDGS service returned error: ${response.status}")
            }

            val results: List<DdgsSearchResult> = response.body()

            logger.debug {
                buildString {
                    appendLine("DDGS search results for query: '$query'")
                    appendLine("Parameters: page=$page, pageSize=$pageSize, region=$region, safeSearch=$safeSearch, timeLimit=$timeLimit")
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

            return formatResults(results)

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

    private fun formatResults(results: List<DdgsSearchResult>): String {
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
