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

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

private val logger = KotlinLogging.logger {}

/**
 * Client for DDGS (DuckDuckGo Search) API server.
 *
 * @param baseUrl The base URL of the DDGS server (default: http://localhost:8000)
 * @param httpClient The Ktor HTTP client to use for requests
 */
class DdgsClient(
    private val baseUrl: String = "http://localhost:8000",
    private val httpClient: HttpClient
) {

    /**
     * Search for text results.
     *
     * @param query The search query
     * @param region The region to search in (e.g., "wt-wt" for worldwide)
     * @param safesearch Safe search level: "on", "moderate", or "off"
     * @param timelimit Time limit for results (e.g., "d" for day, "w" for week, "m" for month)
     * @param maxResults Maximum number of results to return
     * @param backend Search backend to use (e.g., "duckduckgo", "bing", "google")
     * @return List of text search results
     */
    suspend fun searchText(
        query: String,
        region: String? = null,
        safesearch: String? = null,
        timelimit: String? = null,
        maxResults: Int? = null,
        backend: String? = null
    ): List<TextSearchResult> {
        logger.debug { "Searching text: query='$query', region=$region, safesearch=$safesearch" }

        return httpClient.post("$baseUrl/search/text") {
            contentType(ContentType.Application.Json)
            setBody(TextSearchRequest(
                query = query,
                region = region,
                safesearch = safesearch,
                timelimit = timelimit,
                maxResults = maxResults,
                backend = backend
            ))
        }.body<SearchResponse<TextSearchResult>>().results
    }

    /**
     * Search for images.
     *
     * @param query The search query
     * @param region The region to search in
     * @param safesearch Safe search level
     * @param timelimit Time limit for results
     * @param maxResults Maximum number of results to return
     * @return List of image search results
     */
    suspend fun searchImages(
        query: String,
        region: String? = null,
        safesearch: String? = null,
        timelimit: String? = null,
        maxResults: Int? = null
    ): List<ImageSearchResult> {
        logger.debug { "Searching images: query='$query'" }

        return httpClient.post("$baseUrl/search/images") {
            contentType(ContentType.Application.Json)
            setBody(ImageSearchRequest(
                query = query,
                region = region,
                safesearch = safesearch,
                timelimit = timelimit,
                maxResults = maxResults
            ))
        }.body<SearchResponse<ImageSearchResult>>().results
    }

    /**
     * Search for news.
     *
     * @param query The search query
     * @param region The region to search in
     * @param safesearch Safe search level
     * @param timelimit Time limit for results
     * @param maxResults Maximum number of results to return
     * @return List of news search results
     */
    suspend fun searchNews(
        query: String,
        region: String? = null,
        safesearch: String? = null,
        timelimit: String? = null,
        maxResults: Int? = null
    ): List<NewsSearchResult> {
        logger.debug { "Searching news: query='$query'" }

        return httpClient.post("$baseUrl/search/news") {
            contentType(ContentType.Application.Json)
            setBody(NewsSearchRequest(
                query = query,
                region = region,
                safesearch = safesearch,
                timelimit = timelimit,
                maxResults = maxResults
            ))
        }.body<SearchResponse<NewsSearchResult>>().results
    }

    /**
     * Search for videos.
     *
     * @param query The search query
     * @param region The region to search in
     * @param safesearch Safe search level
     * @param timelimit Time limit for results
     * @param maxResults Maximum number of results to return
     * @return List of video search results
     */
    suspend fun searchVideos(
        query: String,
        region: String? = null,
        safesearch: String? = null,
        timelimit: String? = null,
        maxResults: Int? = null
    ): List<VideoSearchResult> {
        logger.debug { "Searching videos: query='$query'" }

        return httpClient.post("$baseUrl/search/videos") {
            contentType(ContentType.Application.Json)
            setBody(VideoSearchRequest(
                query = query,
                region = region,
                safesearch = safesearch,
                timelimit = timelimit,
                maxResults = maxResults
            ))
        }.body<SearchResponse<VideoSearchResult>>().results
    }

    /**
     * Search for books.
     *
     * @param query The search query
     * @param maxResults Maximum number of results to return
     * @return List of book search results
     */
    suspend fun searchBooks(
        query: String,
        maxResults: Int? = null
    ): List<BookSearchResult> {
        logger.debug { "Searching books: query='$query'" }

        return httpClient.post("$baseUrl/search/books") {
            contentType(ContentType.Application.Json)
            setBody(BookSearchRequest(
                query = query,
                maxResults = maxResults
            ))
        }.body<SearchResponse<BookSearchResult>>().results
    }

    /**
     * Check the health status of the DDGS server.
     *
     * @return Health status response
     */
    suspend fun checkHealth(): HealthStatus {
        logger.debug { "Checking DDGS server health" }
        return httpClient.get("$baseUrl/health").body()
    }
}
