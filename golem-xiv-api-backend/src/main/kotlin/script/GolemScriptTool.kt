/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.backend.script

import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.serialization.SerialName

/**
 * Represents a Golem script with its required attributes and content.
 */
@SerialName("ExecuteGolemScript")
@Description($$"""
Executes Kotlin script changing the state of the environment.

The fulfillment of your intent, or the impediment which occurred, will be communicated back to you, but not to other agents involved in the communication. 

Here is the API you can use when writing the script:

<golem-script-api>
$$GOLEM_SCRIPT_API
</golem-script-api>

Here are object instances injected to the script:

<golem-script-api>
val files: Files
val memory: Memory
<golem-script-api>

IMPORTANT: remember to add necessary imports.
IMPORTANT: when writing multiline Kotlin strings containing dollar character, remember to escape them with ${'$'} (otherwise Kotlin compiler will try to resolve them as references)
IMPORTANT: always prefer to use `Files` service to list files (it will skip hidden and ignored files).

The script execution is wrapped in a coroutine, therefore suspended functions can be called directly.

It the last expression of the script is null, or Unit (e.g. function), no information about script execution will be sent back to you. 
If the last expression of the script, e.g. a String, will be send back to you as a tool result.
IMPORTANT: don't use println, the only script output you will see is the last expression of the Golem script. 
"""
)
data class ExecuteGolemScript(
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
