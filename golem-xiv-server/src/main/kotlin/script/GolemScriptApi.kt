/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

// TODO count how much comment, to preserve lines in case of errors here
package com.xemantic.ai.golem.server.script

import kotlinx.serialization.Serializable
import org.neo4j.driver.Result

///** The context window. */
//interface Context {
//    var title: String
////    val startDate: Instant
////    val updateDate: Instant
//    //var replaceThisAssistantMessageWith: String
//}

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
     * Example usage:
     *
     * ```
     * memory.remember {
     *     subject {
     *         type = "Person"
     *         properties(
     *             "name" to "John Smith"
     *         )
     *     }
     *     predicate = "worksAt"
     *     target {
     *         type = "Organization"
     *         properties(
     *             "name" to "Acme",
     *             "hq" to "Berlin"
     *         )
     *     }
     * }
     * ```
     */
    fun remember(block: FactBuilder.() -> Unit)
    fun <T: Any?> query(cypher: String, block: (Result) -> T): T
}

interface FactBuilder {
    fun subject(block: NodeBuilder.() -> Unit)
    /** Naming according to schema.org, e.g. worksFor, memberOf, sameAs */
    var predicate: String?
    fun target(block: NodeBuilder.() -> Unit)
    fun properties(vararg props: Pair<String, Any>)
    /** The sources of the fact */
    var source: String?
    /** Optional confidence level in the 0-1 range, defaults to 1 if not specified */
    var confidence: Double
}

interface NodeBuilder {
    var type: String?
    val additionalTypes: MutableList<String>
    fun properties(vararg props: Pair<String, Any>)
}
