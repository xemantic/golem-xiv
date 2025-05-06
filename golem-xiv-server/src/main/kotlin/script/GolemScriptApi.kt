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
