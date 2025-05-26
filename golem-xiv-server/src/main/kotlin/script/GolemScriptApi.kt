/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script

import kotlinx.serialization.Serializable
import org.neo4j.driver.Result

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
     * Remembers facts.
     *
     * Example usage:
     * ```
     * memory.remember {
     *     val john = node {
     *         type = "Person"
     *         properties(
     *             "name" to "John Smith"
     *             "email" to "john@exemaple.com"
     *         )
     *     }
     *     val acme = node {
     *         type = "Organization"
     *         properties(
     *             "name" to "Acme",
     *             "foundingDate" to LocalDate.of(1987, 4, 1)
     *         )
     *     }
     *     val worksFor = relationship {
     *         subject = john
     *         predicate = "worksFor"
     *         target = acme
     *         source = "Conversation with John"
     *     }
     *     // return ids, so they can be referenced when storing next facts or updating
     *     "john: $john, acme: $acme, worksFor: $worksFor"
     * }
     * ```
     *
     * @param block the memory builder DSL.
     * @return the final String expression of the DSL.
     */
    fun remember(block: MemoryBuilder.() -> String): String
    fun <T: Any?> query(cypher: String, block: (Result) -> T): T
    fun <T: Any?> modify(cypher: String, block: (Result) -> T): T
}

interface MemoryBuilder {
    /**
     * Creates a node.
     *
     * @return node id.
     */
    fun node(block: NodeBuilder.() -> Unit): Long
    /**
     * Creates a relationship.
     *
     * @return relationship id.
     */
    fun relationship(block: RelationshipBuilder.() -> Unit): Long
}

interface WithProperties {
    fun properties(vararg props: Pair<String, Any>)
}

interface NodeBuilder : WithProperties {
    var type: String?
    val additionalTypes: MutableList<String>
}

/** All parameters are required, unless stated otherwise. */
interface RelationshipBuilder : WithProperties {
    /** Subject node id. */
    var subject: Long?
    /** Naming according to schema.org, e.g., worksFor, memberOf, sameAs */
    var predicate: String?
    /** Target node id. */
    var target: Long?
    /** The sources of the fact */
    var source: String?
    /** Optional confidence level in the 0-1 range, defaults to 1 if not specified */
    var confidence: Double?
}
