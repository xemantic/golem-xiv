/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.ai.golem.server.script

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

/**
 * Represents a Golem script with its required attributes and content.
 */
data class GolemScript(
    val purpose: String,
    val code: String
)

/**
 * Extracts all content between `<golem-script purpose="foo"></golem-script>` tags in the given Flow of a text.
 *
 * @return A Flow of GolemScript objects containing the purpose and content of each script tag found
 */
fun Flow<String>.extractGolemScripts(): Flow<GolemScript> = flow {
    val buffer = StringBuilder()

    collect { chunk ->
        buffer.append(chunk)

        // Try to extract all complete script blocks
        extractScripts(buffer, this)
    }
}

/**
 * Helper function to extract all complete script blocks from the buffer and emit them.
 */
private suspend fun extractScripts(
    buffer: StringBuilder,
    emitter: FlowCollector<GolemScript>
) {
    while (true) {
        // Try to find a complete script block
        val startTag = "<golem-script"
        val startTagIndex = buffer.indexOf(startTag)
        if (startTagIndex == -1) return

        val purposeAttr = "purpose=\""
        val purposeAttrIndex = buffer.indexOf(purposeAttr, startTagIndex)
        if (purposeAttrIndex == -1) return

        val purposeStart = purposeAttrIndex + purposeAttr.length
        val purposeEnd = buffer.indexOf("\"", purposeStart)
        if (purposeEnd == -1) return

        val purpose = buffer.substring(purposeStart, purposeEnd)

        val startContentIndex = buffer.indexOf(">", purposeEnd)
        if (startContentIndex == -1) return

        val contentStart = startContentIndex + 1

        val endTag = "</golem-script>"
        val endTagIndex = buffer.indexOf(endTag, contentStart)
        if (endTagIndex == -1) return

        val content = buffer.substring(contentStart, endTagIndex)

        emitter.emit(GolemScript(purpose, content))

        // Remove the processed part from the buffer
        buffer.delete(0, endTagIndex + endTag.length)
    }
}
