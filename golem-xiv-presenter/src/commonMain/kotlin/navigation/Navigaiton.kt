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
