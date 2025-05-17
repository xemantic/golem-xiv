/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script

import com.xemantic.ai.tool.schema.meta.Description
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a Golem script with its required attributes and content.
 */
@SerialName("ExecuteGolemScript")
@Description($$"""
You have the ability to generate Kotlin script code to fulfill any task.

Always use Kotlin script when mathematical calculation is involved.

Here is the API you can use when writing the script:

<golem-script-api>
$$GOLEM_SCRIPT_API
</golem-script-api>

Here are object instances injected to the script:

<golem-script-api>
val context: Context
val files: Files
val browser: WebBrowser
val memory: Memory
<golem-script-api>

IMPORTANT: remember to add necessary imports.
IMPORTANT: when writing multiline Kotlin strings containing dollar character, remember to escape them with ${'$'} (otherwise Kotlin compiler will try to resolve them as references)
IMPORTANT: always prefer to use `Files` service to list files (it will skip hidden and ignored files).

The script execution is wrapped in a coroutine, therefore suspended functions can be called directly.

It the last expression of the script is null, or Unit (e.g. function), no information about script execution will be sent back to you. 
If the last expression of the script will be send back as a tool result.

The last expression of the script can be:
- a String, which will be returned back as a string content to you
- a ByteArray, which will be returned back as respective binary content to you (e.g. JPEG, PNG, PDF), or an error if the content is not supported
- an instance of any other object, which will be converted to String by calling toString() function
- a list of objects of any of the types mentioned above, in such case multiple content elements will be send back to you in order
- a Unit (e.g. function, or ), which will not be returned to you
"""
)
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
