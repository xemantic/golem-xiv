/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import com.xemantic.ai.golem.api.backend.script.Memory
import com.xemantic.ai.golem.api.backend.script.MemoryBuilder
import com.xemantic.ai.golem.api.backend.script.NodeBuilder
import com.xemantic.ai.golem.api.backend.script.RelationshipBuilder
import org.neo4j.driver.Driver
import org.neo4j.driver.Result
import org.neo4j.driver.TransactionContext
import org.neo4j.driver.Values

class DefaultMemory(
    private val driver: Driver
) : Memory {

    override fun remember(
        block: MemoryBuilder.() -> String
    ): String = driver.session().use { session ->
        session.executeWrite { tx ->
            DefaultMemoryBuilder(tx).run(block)
        }
    }

    override fun <T> query(
        cypher: String,
        block: (Result) -> T
    ): T = driver.session().use { session ->
        session.executeRead { tx ->
            val result = tx.run(cypher)
            block(result)
        }
    }

    override fun <T> modify(
        cypher: String,
        block: (Result) -> T
    ): T = driver.session().use { session ->
        session.executeWrite { tx ->
            val result = tx.run(cypher)
            block(result)
        }
    }

}

private class DefaultMemoryBuilder(
    private val tx: TransactionContext
) : MemoryBuilder {

    override fun node(
        block: NodeBuilder.() -> Unit
    ): Long {
        val builder = DefaultNodeBuilder().also(block)
        builder.validate()
        builder.label
        val nodeQuery = $$"""
            CREATE (n$${builder.label})
            SET n += $props
            RETURN ID(n) as id
        """.trimIndent()

        val result = tx.run(
            nodeQuery,
            Values.parameters(
                "props", builder.props
            )
        )

        return result.single()["id"].asLong()
    }

    override fun relationship(
        block: RelationshipBuilder.() -> Unit
    ): Long {
        val builder = DefaultRelationshipBuilder().also(block)
        builder.validate()

        // Can predicate be set as variable, is it cached like a prepared statement?
        val relationshipQuery = $$"""
            MATCH (subject), (target)
            WHERE ID(subject) = $subjectId AND ID(target) = $targetId
            CREATE (subject)-[r:$${builder.predicate}]->(target)
            SET r += $props
            RETURN ID(r) AS id
        """.trimIndent()

        val result = tx.run(
            relationshipQuery,
            mapOf(
                "subjectId" to builder.subject,
                "targetId" to builder.target,
                "predicate" to builder.predicate,
                "props" to builder.props
                // TODO insert confidence
            )
        )

        return result.single()["id"].asLong()
    }

}

private class DefaultNodeBuilder() : NodeBuilder {

    override var type: String? = null
    override val additionalTypes: MutableList<String> = mutableListOf()

    var props: Map<String, Any> = emptyMap()

    override fun properties(vararg props: Pair<String, Any>) {
        this.props = props.toMap()
    }

    fun validate() {
        requireNotNull(type) {
            "node type must be specified"
        }
        requireNotNull(props)
    }

    val label get() = (listOfNotNull(type) + additionalTypes).joinToString(":", prefix = ":")

}

private class DefaultRelationshipBuilder() : RelationshipBuilder {

    override var subject: Long? = null
    override var predicate: String? = null
    override var target: Long? = null
    override var source: String? = null
    override var confidence: Double? = 1.0

    var props: Map<String, Any>? = emptyMap()

    override fun properties(
        vararg props: Pair<String, Any>
    ) {
        this.props = props.toMap()
    }

    fun validate() {
        requireNotNull(subject) { "subject must be set" }
        requireNotNull(predicate) { "predicate must be set" }
        requireNotNull(target) { "target must be set" }
        requireNotNull(source) { "source must be set" }
        requireNotNull(confidence) { "confidence must be set" }
    }

}
