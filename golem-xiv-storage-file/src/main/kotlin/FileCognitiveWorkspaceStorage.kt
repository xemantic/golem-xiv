/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.storage.file

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
        logger.debug {
            "Creating workspace, workspaceId: $workspaceId"
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
            "Creating expression, workspaceId: $workspaceId, expressionId: $expressionId"
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
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        textDelta: String,
        classifier: String
    ) {
        logger.debug {
            "Workspace[$workspaceId]/Expression[$expressionId]/[$phenomenonId]: appending $classifier"
        }
        val file = getPhenomenonFile(workspaceId, expressionId, phenomenonId, classifier)
        file.appendText(textDelta)
    }

    override suspend fun readPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        classifier: String
    ): String {
        logger.debug {
            "Workspace[$workspaceId]/Expression[$expressionId]/[$phenomenonId]: reading $classifier"
        }
        val file = getPhenomenonFile(workspaceId, expressionId, phenomenonId, classifier)
        val text = file.readText()
        return text
    }

    private fun getPhenomenonFile(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        classifier: String
    ): File {
        val workspaceDir = File(storageDir, workspaceId.pad())
        val expressionDir = File(workspaceDir, expressionId.pad())
        val file = File(expressionDir, "${phenomenonId.pad()}-$classifier.md")
        return file
    }

}

private fun Number.pad(): String {
    return this.toString().padStart(6, '0')
}
