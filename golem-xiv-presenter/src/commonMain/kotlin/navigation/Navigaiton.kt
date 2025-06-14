/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Navigation {

    suspend fun navigateTo(target: Target)

    @Serializable
    sealed interface Target {

        @Serializable
        @SerialName("InitiateCognition")
        object InitiateCognition : Target {
            override fun toString(): String = "InitiateCognition"
        }

        @Serializable
        @SerialName("Memory")
        object Memory : Target {
            override fun toString(): String = "Memory"
        }

        @Serializable
        @SerialName("Cognition")
        data class Cognition(
            val id: Long
        ) : Target

        @Serializable
        @SerialName("NotFound")
        data class NotFound(
            val message: String,
            val pathname: String
        ) : Target

    }

}

/**
 * Parses navigation targets, it is using web pathname, but addressing is independent, from the rendering tech
 */
fun parseNavigationTarget(
    pathname: String
): Navigation.Target {

    val segments = pathname.removePrefix("/").split("/").filter { it.isNotEmpty() }

    return when {
        segments.isEmpty() -> Navigation.Target.InitiateCognition
        segments.size == 1 && segments[0] == "memory" -> Navigation.Target.Memory
        segments.size == 2 && segments[0] == "cognitions" -> {
            try {
                val id = segments[1].toLong()
                Navigation.Target.Cognition(id)
            } catch (e: NumberFormatException) {
                Navigation.Target.NotFound(
                    message = "Invalid cognition id (must be a number): ${segments[1]}",
                    pathname = pathname
                )
            }
        }
        else -> Navigation.Target.NotFound(
            message = "No such path: $pathname",
            pathname = pathname
        )
    }

}
