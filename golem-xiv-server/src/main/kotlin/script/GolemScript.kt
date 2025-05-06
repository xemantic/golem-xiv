/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a Golem script with its required attributes and content.
 */
@Serializable
data class GolemScript(
    val purpose: String,
    val code: String
) {

    enum class ExecutionPhase {
        @SerialName("compilation")
        COMPILATION,
        @SerialName("evaluation")
        EVALUATION
    }

    sealed interface Result {

        class Error(val message: String) : Result

        class Value(val value: Any?) : Result

    }

}
