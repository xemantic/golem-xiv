/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
