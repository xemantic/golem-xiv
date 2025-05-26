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

        object KnowledgeGraph : Target {
            override fun toString(): String = "KnowledgeGraph"
        }

        class CognitiveWorkspace(
            val id: String
        ) : Target {
            override fun toString(): String = "CognitiveWorkspace(id=$id)"
        }

    }

}
