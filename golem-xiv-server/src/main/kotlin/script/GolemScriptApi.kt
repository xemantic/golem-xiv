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

interface Memory {

    /**
     * Stores a fact as a relationship between two nodes.
     * @param sourceNode The source node (subject)
     * @param relationship The relationship type
     * @param targetNode The target node (object)
     * @param properties Optional properties to add to the relationship
     * @return ID of the created relationship
     */
    fun storeFact(
        sourceNode: Node,
        relationship: String,
        targetNode: Node,
        properties: Map<String, Any> = emptyMap()
    ): Long

    /**
     * Creates a node in the graph.
     * @param labels Labels to assign to the node
     * @param properties Properties of the node
     * @return The created node
     */
    fun createNode(labels: List<String>, properties: Map<String, Any>): Node

}

data class Node(
    val id: Long,
    val labels: List<String>,
    val properties: Map<String, Any>
)

/** Data class representing a relationship in the graph. */
data class Relationship(
    val id: Long,
    val type: String,
    val properties: Map<String, Any>,
    val startNodeId: Long,
    val endNodeId: Long
)

/** Data class representing a fact in the graph (a relationship between two nodes). */
data class Fact(
    val sourceNode: Node,
    val relationship: Relationship,
    val targetNode: Node
)

/**
 * Enum for specifying relationship direction.
 */
enum class RelationshipDirection {
    OUTGOING,
    INCOMING,
    BOTH
}
