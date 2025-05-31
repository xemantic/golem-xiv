/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.storage.file

import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceStorage
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

class FileCognitiveWorkspaceStorage(
    storageDir: File
) : CognitiveWorkspaceStorage {

    private val logger = KotlinLogging.logger {}

    private val storageDir = storageDir.normalize().absoluteFile.also {
        if (!it.exists()) {
            check(it.mkdirs()) {
                "Could not create dir: $it"
            }
        } else {
            check(it.isDirectory) {
                "Storage path must be a directory, but is a regular file: $it"
            }
        }
    }

    override suspend fun createWorkspace(
        workspaceId: Long,
        conditioning: List<String>
    ) {
        logger.debug { "Creating workspace, id: $workspaceId" }
        val workspaceDir = File(storageDir, "$workspaceId")
        workspaceDir.mkdir()
        val conditioningDir = File(workspaceDir, "_conditioning")
        conditioningDir.mkdir()
        conditioning.forEachIndexed { index, prompt ->
            val promptFile = File(conditioningDir, "$index.md")
            promptFile.writeText(prompt)
        }
    }

    override suspend fun addExpression(
        workspaceId: Long,
        expressionId: Long,
        phenomena: List<Phenomenon>
    ) {
        logger.debug { "Adding expression, workspaceId: $workspaceId, expressionId: $expressionId" }
        val workspaceDir = File(storageDir, "$workspaceId")
        val expressionDir = File(workspaceDir, "$expressionId")
        expressionDir.mkdir()
        phenomena.forEach { phenomenon ->
            val id = phenomenon.id
            when (phenomenon) {
                is Phenomenon.Text -> {
                    val file = File(workspaceDir, "$id.md")
                    file.writeText(phenomenon.text)
                }
                is Phenomenon.Intent -> {
                    // TODO add padding for sorting
                    val purposeFile = File(expressionDir, "$id-intent-purpose.md")
                    val codeFile = File(expressionDir, "$id-intent-code.kts")
                    val systemIdFile = File(expressionDir, "$id-intent-systemId.txt")
                    purposeFile.writeText(phenomenon.purpose)
                    codeFile.writeText(phenomenon.code)
                    systemIdFile.writeText(phenomenon.systemId)
                }
                is Phenomenon.Fulfillment -> {

                }
                is Phenomenon.Impediment -> {

                }
                else -> IllegalStateException("Unsupported phenomenon")
            }
        }
    }

    override suspend fun append(phenomena: List<Phenomenon>) {
        TODO("Not yet implemented")
    }

    override suspend fun commit(workspaceId: Long, expressionId: Long) {
        TODO("Not yet implemented")
    }

}
