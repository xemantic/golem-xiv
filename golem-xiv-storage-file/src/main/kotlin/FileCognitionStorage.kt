/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.storage.file

import com.xemantic.ai.golem.api.backend.CognitionStorage
import com.xemantic.ai.golem.api.backend.StorageType
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

class FileCognitionStorage(
    storageDir: File
) : CognitionStorage {

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

    override suspend fun createCognition(
        cognitionId: Long,
        constitution: List<String>
    ) {
        logger.debug {
            "Cognition[$cognitionId]: creating storage dirs and writing constitution"
        }
        val cognitionDir = File(storageDir, cognitionId.pad())
        cognitionDir.mkdir()
        val constitutionDir = File(cognitionDir, "000000_constitution")
        constitutionDir.mkdir()
        constitution.forEachIndexed { index, prompt ->
            val promptFile = File(constitutionDir, "${index.pad()}.md")
            promptFile.writeText(prompt)
        }
    }

    override suspend fun createExpression(
        cognitionId: Long,
        expressionId: Long
    ) {

        logger.debug {
            "Cognition[$cognitionId]/Expression[$expressionId]: creating storage dirs"
        }

        val cognitionDir = File(storageDir, cognitionId.pad())
        val expressionDir = File(cognitionDir, expressionId.pad())
        expressionDir.mkdir()
    }

//    // TODO is it being used at tall?
//    override suspend fun addExpression(
//        cognitionId: Long,
//        expressionId: Long,
//        phenomena: List<Phenomenon>
//    ) {
//        logger.debug { "Adding expression, cognitionId: cognitionId, expressionId: $expressionId" }
//        val cognitionDir = File(storageDir, "cognitionId")
//        val expressionDir = File(cognitionDir, "$expressionId")
//        expressionDir.mkdir()
//        phenomena.forEach { phenomenon ->
//            val id = phenomenon.id
//            when (phenomenon) {
//                is Phenomenon.Text -> {
//                    val file = File(cognitionDir, "$id.md")
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
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        type: StorageType
    ): File {
        val cognitionDir = File(storageDir, cognitionId.pad())
        val expressionDir = File(cognitionDir, expressionId.pad())
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
