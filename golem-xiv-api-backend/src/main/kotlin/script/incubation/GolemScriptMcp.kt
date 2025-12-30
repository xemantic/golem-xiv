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

package com.xemantic.ai.golem.api.backend.script.incubation

import kotlinx.serialization.Serializable

interface Mcp {

    suspend fun servers(): List<Server>

    interface Server {

        suspend fun tools(): List<Tool>

    }

    interface Tool {

        val name: String
        val description: String?
        val inputSchema: String
        val annotations: Annotations?

        suspend fun call(arguments: String): String

        @Serializable
        data class Annotations(
            val title: String?,
            val readOnlyHint: Boolean?,
            val destructiveHint: Boolean?,
            val idempotentHint: Boolean?,
            val openWorldHint: Boolean?
        )

    }

    sealed interface Content {

        @Serializable
        data class Text(
            val content: String
        ) : Content

        @Serializable
        data class Image(
            val content: String
        ) : Content

    }

}
