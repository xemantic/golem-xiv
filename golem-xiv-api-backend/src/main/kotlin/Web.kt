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

package com.xemantic.ai.golem.api.backend

interface SearchProvider {

    /**
     * Performs a web search and returns results as Markdown.
     *
     * Supports multiple search providers:
     * - "ddgs" or null: Use local DDGS service (free, default)
     * - "anthropic": Use Anthropic WebSearch tool (expensive but high-quality)
     *
     * @param query The search query
     * @param page Page number (default 1)
     * @param pageSize Number of results per page (default 10)
     * @param region Search region (default "us-en")
     * @param safeSearch Safety level: "on", "moderate", "off" (default "moderate")
     * @param timeLimit Time filter: "d" (day), "w" (week), "m" (month), "y" (year), or null
     * @return Markdown-formatted search results
     */

    suspend fun search(
        query: String,
        page: Int = 1,
        pageSize: Int = 10,
        region: String = "us-en",
        safeSearch: String = "moderate",
        timeLimit: String? = null
    ): String

}
