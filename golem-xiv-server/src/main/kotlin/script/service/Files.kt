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

package com.xemantic.ai.golem.server.script.service

import com.xemantic.ai.golem.server.script.Files
import kotlinx.io.IOException
import java.io.File

class DefaultFiles : Files {

    override fun readText(
        vararg paths: String
    ): List<String> = paths.map { File(it).readText() }

    override fun readBinary(
        vararg paths: String
    ): List<ByteArray> = paths.map { File(it).readBytes() }

    override fun create(path: String, content: String) {
        File(path).ensureParentDir().writeText(content)
    }

    override fun create(path: String, content: ByteArray) {
        File(path).ensureParentDir().writeBytes(content)
    }

}

private fun File.ensureParentDir(): File {
    val parent = parentFile
    if (parent.exists()) {
        if (!parent.isDirectory) {
            throw IllegalStateException("Cannot create because parent path is not a directory: $this")
        }
    } else {
        if (!parentFile.exists()) {
            throw IOException("Cannot create parent directory of file: $this")
        }
    }
    return this
}
