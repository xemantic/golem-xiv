/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.backend.script

import com.xemantic.ai.golem.api.PhenomenalExpression
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.neo4j.driver.Result
import kotlin.time.Instant

interface Mind {
    suspend fun currentCognition(): Cognition
    suspend fun getCognition(id: Long): Cognition
}

interface Cognition {
    val id: Long
    val initiationMoment: Instant
    val parentId: Long?
    //    suspend fun getConditioning()
//    suspend fun setConditioning(conditioning: String)
    suspend fun getTitle(): String?
    suspend fun setTitle(title: String?)
    suspend fun getSummary(): String?
    suspend fun setSummary(summary: String?)
    fun expressions(): Flow<PhenomenalExpression>

    enum class State {
        OPEN,
        INTERACTION_PENDING,
        CONCLUDED
    }

}

data class RecursiveCognitionInitiator(
    val conditioning: String,
    val initialPhenomena: List<Any>
)

@Serializable
data class FileEntry(val path: String, val isDirectory: Boolean)

/** Note: create functions will also create parent directories and delete will work recursively on directories */
interface Files {
    /** Note: if .gitignore files are present, then they will determine hidden files */
    suspend fun list(
        dir: String,
        depth: Int = 0,
        excludeHidden: Boolean = false
    ): Flow<FileEntry>
    suspend fun read(path: String): String
    suspend fun readBinary(path: String): ByteArray
    suspend fun create(path: String, content: String)
    suspend fun create(path: String, content: ByteArray)
    suspend fun exists(path: String): Boolean
    suspend fun delete(path: String): Boolean
}

interface Http {
    /**
     * Example usage with resource closing:
     *
     * ```
     * val json = http.client {
     *     install(ContentNegotiation) {
     *         json()
     *     }
     * }.use {
     *     it.get("https://example.com/api/records").bodyAsText()
     * }
     * ```
     */
    fun client(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient
}

interface Memory {
    /**
     * Remembers facts.
     *
     * Example use case:
     *
     * 1. initial intent - search for existing nodes.
     * 2. following intent - remember the fact:
     *
     * ```
     * memory.remember {
     *     val john = 42L // this entry was matched and id is used
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
     *     // return ids, so they can be referenced when storing subsequent facts or updating them
     *     "acme: $acme, worksFor: $worksFor"
     * }
     * ```
     *
     * Note: in case of an Exception, the whole transaction is rolled back.
     *
     * @param block the memory builder DSL.
     * @return the final String expression of the DSL.
     */
    suspend fun remember(block: MemoryBuilder.() -> String): String
    suspend fun <T: Any?> query(cypher: String, block: (Result) -> T): T
    suspend fun <T: Any?> modify(cypher: String, block: (Result) -> T): T
}

interface MemoryBuilder {
    /**
     * Creates a node.
     * @return node id.
     */
    fun node(block: NodeBuilder.() -> Unit): Long
    /**
     * Creates a relationship.
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
