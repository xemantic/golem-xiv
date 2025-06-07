/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.navigation

interface Navigation {

    suspend fun navigate(target: Target)

    sealed interface Target {

        object Memory : Target {
            override fun toString(): String = "Memory"
        }

        data class Cognition(
            val id: Long
        ) : Target

    }

}

fun parseNavigationTarget(
    pathname: String
): Navigation.Target {
    val segments = pathname.removePrefix("/").split("/").filter { it.isNotEmpty() }

    return when {
        segments.size == 2 && segments[0] == "cognition" -> {
            val id = segments[1].toLongOrNull()
                ?: throw IllegalArgumentException("Invalid cognition id: ${segments[1]}")
            Navigation.Target.Cognition(id = id)
        }

        // Future patterns can be added here
        // segments.size == 2 && segments[0] == "project" -> { ... }

        else -> throw IllegalArgumentException("Unknown pathname pattern: $pathname")
    }
}
