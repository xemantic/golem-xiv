/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

// TODO count how much comment, to preserve lines in case of errors here
package com.xemantic.ai.golem.server.script

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** The context window. */
interface Context {
    var title: String
//    val startDate: Instant
//    val updateDate: Instant
    //var replaceThisAssistantMessageWith: String
}

/** Note: create functions will also mkdirs parents. */
interface Files {
    /** */
    fun list(dir: String): List<FileEntry>
    fun readText(vararg paths: String): List<String>
    fun readBinary(vararg paths: String): List<ByteArray>
    fun create(path: String, content: String)
    fun create(path: String, content: ByteArray)
}

@Serializable
data class FileEntry(val path: String, val isDirectory: Boolean)

interface WebBrowser {
    /** @return given [url] as Markdown. */
    suspend fun open(url: String): String
}
