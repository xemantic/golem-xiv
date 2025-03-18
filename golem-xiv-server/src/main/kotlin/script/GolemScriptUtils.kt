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

/**
 * Extracts content between <golem-script> tags from the given text.
 *
 * @param text The text to scan for golem script tags
 * @return The content between the tags, or null if no tags are found
 */
fun extractGolemScript(text: String): String? {
    val startTag = "<golem-script>"
    val endTag = "</golem-script>"

    val startIndex = text.indexOf(startTag)
    if (startIndex == -1) return null

    val contentStartIndex = startIndex + startTag.length
    val endIndex = text.indexOf(endTag, contentStartIndex)
    if (endIndex == -1) return null

    return text.substring(contentStartIndex, endIndex).trim()
}
