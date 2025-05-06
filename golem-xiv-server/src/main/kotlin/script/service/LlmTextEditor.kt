/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script.service

import com.xemantic.ai.golem.server.script.candidate.LlmTextEditor

class DefaultLlmTextEditor : LlmTextEditor {

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

    override suspend fun getResourceInfo(resource: String): LlmTextEditor.ResourceInfo {
        TODO("Not yet implemented")
    }
}