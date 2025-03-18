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

import com.xemantic.ai.golem.server.SYSTEM_PROMPT
import com.xemantic.ai.golem.server.environmentContext
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
    open class Text(val text: String) : Content()

    /**
     * The binary content.
     * Note: it will be passed back to the LLM with detected media type and proper encoding.
     */
    @Serializable
    class Binary(val data: ByteArray) : Content()

    /**
     * The
     */
    @Serializable
    class TextFile(
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
interface RecursiveContext {

    /**
     * If binary content is returned, the media type detection will try to determine
     * if it is an image or a document to be sent back to LLM as a tool result.
     *
     * @param kotlinScriptServiceApi optional API which can be used by kotlin script, if omitted, this default API will be provided.
     */
    suspend fun start(
        system: String = SYSTEM_PROMPT,
        environmentContext: String = environmentContext(),
//        kotlinScriptServiceApi: String? = GOLEM_SCRIPT_SERVICE_API,
        additionalSystemPrompt: String? = null,
        initialConversation: List<Message>? = null,
        cacheAdditionalSystemPrompt: Boolean = false
    ): List<Content>

}

/**
 * An interface designed for LLMs to efficiently edit text resources asynchronously.
 */
interface LlmTextEditor {

    /**
     * Replaces text in the specified resource
     * @param resource URI/path to the file or resource
     * @param oldText Text to be replaced
     * @param newText Text to replace with
     * @param replaceAll Whether to replace all occurrences or just the first one
     * @return Operation identifier for undo functionality
     */
    suspend fun replaceText(
        resource: String,
        oldText: String,
        newText: String,
        replaceAll: Boolean = false
    ): String

    /**
     * Inserts text at a location identified by a pattern
     * @param resource URI/path to the file or resource
     * @param pattern Text pattern to locate the insertion point
     * @param textToInsert Text to insert
     * @param insertBefore Whether to insert before or after the pattern
     * @return Operation identifier for undo functionality
     */
    suspend fun insertAtPattern(
        resource: String,
        pattern: String,
        textToInsert: String,
        insertBefore: Boolean = false
    ): String

    /**
     * Inserts text at a specific line number
     * @param resource URI/path to the file or resource
     * @param lineNumber Line where text should be inserted (1-based index)
     * @param textToInsert Text to insert
     * @return Operation identifier for undo functionality
     */
    suspend fun insertAtLine(
        resource: String,
        lineNumber: Int,
        textToInsert: String
    ): String

    /**
     * Removes specified text from a resource
     * @param resource URI/path to the file or resource
     * @param textToRemove Text to remove
     * @param removeAll Whether to remove all occurrences or just the first one
     * @return Operation identifier for undo functionality
     */
    suspend fun removeText(
        resource: String,
        textToRemove: String,
        removeAll: Boolean = false
    ): String

    /**
     * Modifies text between two markers
     * @param resource URI/path to the file or resource
     * @param startMarker Text that marks the beginning of the section to modify
     * @param endMarker Text that marks the end of the section to modify
     * @param transformation Function that transforms the text between markers
     * @return Operation identifier for undo functionality
     */
    suspend fun modifyBetween(
        resource: String,
        startMarker: String,
        endMarker: String,
        transformation: suspend (String) -> String
    ): String

    /**
     * Wraps specified text with start and end wrappers
     * @param resource URI/path to the file or resource
     * @param textToWrap Text to be wrapped
     * @param wrapperStart Text to insert before the wrapped text
     * @param wrapperEnd Text to insert after the wrapped text
     * @return Operation identifier for undo functionality
     */
    suspend fun wrapSelection(
        resource: String,
        textToWrap: String,
        wrapperStart: String,
        wrapperEnd: String
    ): String

    /**
     * Applies a transformation to all matches of a regex pattern
     * @param resource URI/path to the file or resource
     * @param regex Regular expression to match text
     * @param transformation Function to transform each match
     * @return Operation identifier for undo functionality
     */
    suspend fun applyToPattern(
        resource: String,
        regex: Regex,
        transformation: suspend (MatchResult) -> String
    ): String

    /**
     * Undoes a specific operation by its identifier
     * @param operationId The identifier of the operation to undo
     * @return Whether the undo was successful
     */
    suspend fun undo(operationId: String): Boolean

    /**
     * Reads the content of specified resources. Each resource will be enclosed between
     * <resource id="resource_id"></resource>
     * @param resource URI/path to the file or resource
     * @return The content as a string
     */
    suspend fun readResources(vararg resources: String): String

    /**
     * Writes content to a resource
     * @param resource URI/path to the file or resource
     * @param content The content to write
     * @return Whether the write operation was successful
     */
    suspend fun writeResource(resource: String, content: String): Boolean

    /**
     * Finds the line number where a pattern first appears
     * @param resource URI/path to the file or resource
     * @param pattern Text to search for
     * @return Line number (1-based) or -1 if not found
     */
    suspend fun findLineNumber(resource: String, pattern: String): Int

    /**
     * Gets metadata about a resource
     * @param resource URI/path to the file or resource
     * @return Information about the resource
     */
    suspend fun getResourceInfo(resource: String): ResourceInfo

    /**
     * Data class representing metadata about a resource
     */
    data class ResourceInfo(
        val exists: Boolean,
        val size: Long,
        val lastModified: Long,
        val isReadOnly: Boolean,
        val extension: String,
        val mimeType: String?
    )

}

interface WebBrowser {

    /**
     * Opens given URL.
     *
     * @param url the URL to open.
     * @param windowId the window ID to use.
     * @return 2 element list, where the first element is the URL content, either binary or text, and the second element represent unique id of the window being open to server this request.
     */
    suspend fun open(
        url: String,
        windowId: Int? = null
    ): Content

    suspend fun screenshot(): Content.Binary

}

interface Shell {

    /**
     * Executes given shell command.
     *
     * @param command the command to execute.
     * @param workingDir the working directory.
     * @param timeout the timeout in seconds.
     */
    suspend fun execute(
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
