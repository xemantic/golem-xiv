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
package com.xemantic.ai.golem.service

import com.xemantic.ai.golem.SYSTEM_PROMPT
import com.xemantic.ai.golem.environmentContext
import kotlinx.serialization.Serializable

/**
 * Represents content element returned back to the LLM.
 */
@Serializable
sealed class Content {

    /**
     * The textual content.
     */
    @Serializable
    class Text(val text: String) : Content()

    /**
     * The binary content.
     * Note: it will be passed back to the LLM with detected media type and proper encoding.
     */
    @Serializable
    class Binary(val data: ByteArray) : Content()

    class TextFileContent(
        val path: String,
        val text: String,
        val range: IntRange? = null
    ) : Content()

}

class Message(
    val role: Role,
    val content: List<Content>
) {

    enum class Role {
        USER,
        ASSISTANT
    }

}

/**
 * Starts a recursive version of yourself with a fresh token window. If there is any tool use involved,
 */
interface RecursiveAgentService {

    /**
     * If binary content is returned, the media type detection will try to determine
     * if it is an image or a document to be sent back to LLM as a tool result.
     *
     * @param kotlinScriptServiceApi optional API which can be used by kotlin script, if omitted, this default API will be provided.
     */
    suspend fun start(
        system: String = SYSTEM_PROMPT,
        environmentContext: String = environmentContext(),
        kotlinScriptServiceApi: String? = com.xemantic.ai.golem.GOLEM_SCRIPT_SERVICE_API,
        additionalSystemPrompt: String? = null,
        initialConversation: List<Message>? = null,
        cacheAdditionalSystemPrompt: Boolean = false
    ): List<Content>

}

interface FileService {

    /**
     * Creates a file at given `path`.
     * Note: if parent directories don't exist, it will create them.
     *
     * @param path the absolute file path.
     * @param content the file content.
     * @param base64 the indicator if a file should be treated as base64.
     */
    suspend fun createFile(
        path: String,
        content: String,
        base64: Boolean = false
    )

    /**
     * Reads binary files, so they can be analyzed.
     *
     * - Image formats supported by Claude will be provided according to their respective content types. " +
     * - The contents of other types of files will transferred as Base64 encoded text content."
     *
     * @param paths the list of absolute file paths.
     */
    suspend fun readBinaryFiles(
        paths: List<String>
    ): List<Content.Binary>

}

/*
{
    "properties": {
        "command": {
            "description": "The commands to run. Allowed options are: `view`, `create`, `str_replace`, `insert`, `undo_edit`.",
            "enum": [ "create", "str_replace", "insert", "undo_edit"],
            "type": "string",
        },
        "insert_line": {
            "description": "Required parameter of `insert` command. The `new_str` will be inserted AFTER the line `insert_line` of `path`.",
            "type": "integer",
        },
        "new_str": {
            "description": "Optional parameter of `str_replace` command containing the new string (if not given, no string will be added). Required parameter of `insert` command containing the string to insert.",
            "type": "string",
        },
        "old_str": {
            "description": "Required parameter of `str_replace` command containing the string in `path` to replace.",
            "type": "string",
        },

    },
    "required": ["command", "path"],
    "type": "object",
}

 */

/**
 * All the `path` parameters represent absolute paths, e.g. `/repo/file.py` or `/repo`.
 */
interface StringEditorService {

    /**
     * Views the file at given path.
     *
     * @param path the absolute file path.
     * @param range If none is given, the full file is shown. If provided, the file will be shown in the indicated line number range, e.g. [11, 12] will show lines 11 and 12. Indexing at 1 to start. Setting `[start_line, -1]` shows all lines from `start_line` to the end of the file.
     */
    suspend fun view(
        path: String,
        range: IntRange? = null
    ): Content.TextFileContent

    /**
     * Creates the file.
     *
     * @param path the absolute file path.
     */
    suspend fun create(
        path: String,
        content: String
    )

    /**
     * @param newStr The `new_str` will be inserted AFTER the line `insert_line` of `path`.
     */
    suspend fun replace(
        path: String,
        oldStr: String,
        newStr: String? = null
    )

    /**
     *
     * @param insertLine
     * @param newStr The new string which will be inserted AFTER the line [insertLine] of [path].
     */
    suspend fun insert(
        path: String,
        insertLine: Int,
        newStr: String? = null
    )

}

interface BashService {

    /**
     * Executes given shell command.
     *
     * @param command the command to execute.
     * @param workingDir the working directory.
     * @param timeout the timeout in seconds.
     */
    suspend fun executeShellCommand(
        command: String,
        workingDir: String = ".",
        timeout: Int = 60
    ): String

}

//@Description("""
//
//""")
//data class ReadBinaryFiles(
//    @Description(
//        "The list of absolute file paths. " +
//                "The order of file content in tool result will much the order of file paths."
//    )
//    val paths: List<String>,
//    @Description(
//        "Indicates whether tool result of reading the files should be cached or not. " +
//                "Defaults to false if omitted."
//    )
//    val cache: Boolean? = false
//) : ToolInput
//
//@Description("""
//Reads text files from human's machine.
//
//- All the files will be packaged into as single text content, where each file is surrounded by the <file></file> tags.
//- Each <file> tag will also have the path attribute.
//- In case of an error reading file a possible error attribute will pop up containing error message.
//""")
//data class ReadFiles(
//    @Description("The list of absolute file paths.")
//    val paths: List<String>,
//    @Description(
//        "Indicates whether tool result of reading the files should be cached or not. (Defaults to false if omitted)."
//    )
//    val cache: Boolean? = false
//) : ToolInput
//
