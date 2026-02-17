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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Request DTOs

@Serializable
data class TextSearchRequest(
    val query: String,
    val region: String? = null,
    val safesearch: String? = null,
    val timelimit: String? = null,
    @SerialName("max_results")
    val maxResults: Int? = null,
    val backend: String? = null
)

@Serializable
data class ImageSearchRequest(
    val query: String,
    val region: String? = null,
    val safesearch: String? = null,
    val timelimit: String? = null,
    @SerialName("max_results")
    val maxResults: Int? = null
)

@Serializable
data class NewsSearchRequest(
    val query: String,
    val region: String? = null,
    val safesearch: String? = null,
    val timelimit: String? = null,
    @SerialName("max_results")
    val maxResults: Int? = null
)

@Serializable
data class VideoSearchRequest(
    val query: String,
    val region: String? = null,
    val safesearch: String? = null,
    val timelimit: String? = null,
    @SerialName("max_results")
    val maxResults: Int? = null
)

@Serializable
data class BookSearchRequest(
    val query: String,
    @SerialName("max_results")
    val maxResults: Int? = null
)

// Response DTOs

@Serializable
data class SearchResponse<T>(
    val results: List<T>
)

@Serializable
data class TextSearchResult(
    val title: String,
    val href: String,
    val body: String
)

@Serializable
data class ImageSearchResult(
    val title: String,
    val image: String,
    val thumbnail: String,
    val url: String,
    val height: Int,
    val width: Int,
    val source: String
)

@Serializable
data class NewsSearchResult(
    val date: String,
    val title: String,
    val body: String,
    val url: String,
    val image: String? = null,
    val source: String
)

@Serializable
data class VideoImages(
    val large: String,
    val medium: String,
    val motion: String,
    val small: String
)

@Serializable
data class VideoSearchResult(
    val title: String,
    val description: String,
    val duration: String,
    @SerialName("embed_url")
    val embedUrl: String,
    val images: VideoImages,
    val uploader: String
)

@Serializable
data class BookSearchResult(
    val title: String,
    val author: String,
    val publisher: String,
    val info: String,
    val url: String,
    val thumbnail: String
)

@Serializable
data class HealthStatus(
    val status: String
)
