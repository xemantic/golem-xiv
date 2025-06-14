/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.storage.file

import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceStorage
import com.xemantic.ai.golem.api.backend.StorageType
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
        logger.debug {
            "WorkspaceId[$workspaceId]: creating storage dirs and writing conditioning"
        }
        val workspaceDir = File(storageDir, workspaceId.pad())
        workspaceDir.mkdir()
        val conditioningDir = File(workspaceDir, "000000_conditioning")
        conditioningDir.mkdir()
        conditioning.forEachIndexed { index, prompt ->
            val promptFile = File(conditioningDir, "${index.pad()}.md")
            promptFile.writeText(prompt)
        }
    }

    override suspend fun createExpression(
        workspaceId: Long,
        expressionId: Long
    ) {

        logger.debug {
            "WorkspaceId[$workspaceId]/Expression[$expressionId]: creating storage dirs"
        }

        val workspaceDir = File(storageDir, workspaceId.pad())
        val expressionDir = File(workspaceDir, expressionId.pad())
        expressionDir.mkdir()
    }

//    // TODO is it being used at tall?
//    override suspend fun addExpression(
//        workspaceId: Long,
//        expressionId: Long,
//        phenomena: List<Phenomenon>
//    ) {
//        logger.debug { "Adding expression, workspaceId: $workspaceId, expressionId: $expressionId" }
//        val workspaceDir = File(storageDir, "$workspaceId")
//        val expressionDir = File(workspaceDir, "$expressionId")
//        expressionDir.mkdir()
//        phenomena.forEach { phenomenon ->
//            val id = phenomenon.id
//            when (phenomenon) {
//                is Phenomenon.Text -> {
//                    val file = File(workspaceDir, "$id.md")
//                    file.writeText(phenomenon.text)
//                }
//                is Phenomenon.Intent -> {
//                    // TODO add padding for sorting
//                    val purposeFile = File(expressionDir, "$id-intent-purpose.md")
//                    val codeFile = File(expressionDir, "$id-intent-code.kts")
//                    val systemIdFile = File(expressionDir, "$id-intent-systemId.txt")
//                    purposeFile.writeText(phenomenon.purpose)
//                    codeFile.writeText(phenomenon.code)
//                    systemIdFile.writeText(phenomenon.systemId)
//                }
//                is Phenomenon.Fulfillment -> {
//
//                }
//                is Phenomenon.Impediment -> {
//
//                }
//                else -> IllegalStateException("Unsupported phenomenon")
//            }
//        }
//    }

    override suspend fun append(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        textDelta: String,
        type: StorageType
    ) {

        logger.trace {
            "Cognition[$cognitionId]/Expression[$expressionId]/Phenomenon[$phenomenonId]: appending $type"
        }

        val file = getPhenomenonFile(cognitionId, expressionId, phenomenonId, type)
        file.appendText(textDelta)
    }

    override suspend fun readPhenomenonComponent(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        type: StorageType
    ): String {

        logger.debug {
            "Cognition[$cognitionId]/Expression[$expressionId]/[$phenomenonId]: reading $type"
        }

        val file = getPhenomenonFile(cognitionId, expressionId, phenomenonId, type)
        val text = file.readText()
        return text
    }

    private fun getPhenomenonFile(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        type: StorageType
    ): File {
        val workspaceDir = File(storageDir, workspaceId.pad())
        val expressionDir = File(workspaceDir, expressionId.pad())
        val extension = when (type) {
            StorageType.INTENT_CODE -> "kts"
            StorageType.SYSTEM_ID -> "txt"
            else -> "md"
        }
        val name = "${phenomenonId.pad()}-${type.name.lowercase()}.$extension"
        val file = File(expressionDir, name)
        return file
    }

}

private fun Number.pad(): String {
    return this.toString().padStart(6, '0')
}
