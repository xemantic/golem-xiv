/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script.candidate

import com.xemantic.ai.golem.server.SYSTEM_PROMPT
import com.xemantic.ai.golem.server.environmentContext

interface Memory {
    // Entity/fact management
    suspend fun storeFact(fact: Fact): String  // Returns ID of stored fact
    suspend fun getFact(id: String): Fact?
    suspend fun updateFact(id: String, fact: Fact): Boolean

    // Relationship management
    suspend fun createRelationship(sourceId: String, relationshipType: String, targetId: String): String
    suspend fun getRelationships(entityId: String): List<Relationship>

    // Query capabilities
    suspend fun query(queryString: String): QueryResult

    // Episodic memory capabilities
    suspend fun rememberInteraction(interaction: Interaction)
    suspend fun getRelevantMemories(context: String, limit: Int = 5): List<Memory>

    // Optional: Vector embedding for similarity search
    suspend fun findSimilar(description: String, limit: Int = 5): List<Fact>
}

// Supporting data classes
data class Fact(
    val content: String,
    val metadata: Map<String, Any> = emptyMap(),
    val confidence: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class Relationship(
    val sourceId: String,
    val type: String,
    val targetId: String,
    val metadata: Map<String, Any> = emptyMap(),
    val confidence: Float = 1.0f
)

data class Interaction(
    val type: String,  // e.g., "user_message", "agent_action"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
)

data class QueryResult(
    val facts: List<Fact> = emptyList(),
    val relationships: List<Relationship> = emptyList(),
    val rawResult: Any? = null
)

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
        delay: Long = 0,// milliseconds
        system: String = SYSTEM_PROMPT,
        environmentContext: String = environmentContext(),
//        kotlinScriptServiceApi: String? = GOLEM_SCRIPT_SERVICE_API,
        additionalSystemPrompt: String? = null,
        initialPrompt: List<Any>,
        cacheAdditionalSystemPrompt: Boolean = false,
    ): Any

    // TODO add scheduled recursive agent

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

    suspend fun open(
        url: String,
        windowId: Int? = null
    ): String

    suspend fun openAsBinary(
        url: String,
        windowId: Int? = null
    ): ByteArray

    suspend fun screenshot(): ByteArray

    fun close()

}

interface WebBrowsers {

    fun webBrowser()

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

interface Scheduler {

    /**
     * Waits given number of milliseconds and then continues with the continuation prompt.
     *
     * Note: this expression should always be scheduled as the last one inside the <golem-script> tag.
     */
    suspend fun wait(
        milliseconds: Long,
        continuationPrompt: String
    )

}

