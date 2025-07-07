/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
