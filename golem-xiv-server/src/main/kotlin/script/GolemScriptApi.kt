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

/**
 * Represents the current context window.
 */
interface Context {

    /**
     * The title of this context window.
     */
    var title: String

}

interface Files {

    fun readText(
        vararg paths: String
    ): List<String>

    fun readBinary(
        vararg paths: String
    ): List<ByteArray>

    /**
     * Creates a text file.
     *
     * @param path the absolute file path.
     * @param content the file content.
     */
    fun create(
        path: String,
        content: String,
    )

    /**
     * Creates a binary file.
     *
     * @param path the absolute file path.
     * @param content the file content.
     */
    fun create(
        path: String,
        content: ByteArray,
    )

}
