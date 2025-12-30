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

package com.xemantic.ai.golem.core.script

import com.xemantic.ai.golem.api.backend.script.ExecuteGolemScript
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

/**
 * Extracts all content between `<golem-script purpose="foo"></golem-script>` tags in the given Flow of a text.
 *
 * @return A Flow of GolemScript objects containing the purpose and content of each script tag found
 */
fun Flow<String>.extractGolemScripts(): Flow<ExecuteGolemScript> = flow {
    val buffer = StringBuilder()
    collect { chunk ->
        buffer.append(chunk)
        extractScripts(buffer, this)
    }
}

/**
 * Helper function to extract all complete script blocks from the buffer and emit them.
 */
private suspend fun extractScripts(
    buffer: StringBuilder,
    emitter: FlowCollector<ExecuteGolemScript>
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

        val content = buffer.substring(contentStart, endTagIndex).trim()

        emitter.emit(ExecuteGolemScript(purpose, content))

        // Remove the processed part from the buffer
        buffer.delete(0, endTagIndex + endTag.length)
    }
}
