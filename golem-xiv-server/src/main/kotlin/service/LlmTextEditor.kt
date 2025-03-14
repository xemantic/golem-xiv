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

package com.xemantic.ai.golem.server.service

class DefaultLLmTextEditorService : LlmTextEditorService {

    override suspend fun replaceText(
        resource: String,
        oldText: String,
        newText: String,
        replaceAll: Boolean
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun insertAtPattern(
        resource: String,
        pattern: String,
        textToInsert: String,
        insertBefore: Boolean
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun insertAtLine(
        resource: String,
        lineNumber: Int,
        textToInsert: String
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun removeText(
        resource: String,
        textToRemove: String,
        removeAll: Boolean
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun modifyBetween(
        resource: String,
        startMarker: String,
        endMarker: String,
        transformation: suspend (String) -> String
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun wrapSelection(
        resource: String,
        textToWrap: String,
        wrapperStart: String,
        wrapperEnd: String
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun applyToPattern(
        resource: String,
        regex: Regex,
        transformation: suspend (MatchResult) -> String
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun undo(operationId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun readResources(vararg resources: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun writeResource(resource: String, content: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun findLineNumber(resource: String, pattern: String): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getResourceInfo(resource: String): LlmTextEditorService.ResourceInfo {
        TODO("Not yet implemented")
    }
}