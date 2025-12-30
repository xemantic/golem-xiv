/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.golem.neo4j

import com.xemantic.ai.golem.api.backend.script.Memory
import com.xemantic.ai.golem.api.backend.script.MemoryBuilder
import com.xemantic.ai.golem.api.backend.script.NodeBuilder
import com.xemantic.ai.golem.api.backend.script.RelationshipBuilder
import com.xemantic.neo4j.driver.Neo4jOperations
import com.xemantic.neo4j.driver.TransactionContext
import kotlinx.coroutines.flow.Flow
import org.neo4j.driver.Record

class Neo4jMemory(
    private val neo4j: Neo4jOperations,
    private val cognitionId: Long,
    private val fulfillmentId: Long
) : Memory {

    override suspend fun <T> remember(
        block: suspend MemoryBuilder.() -> T
    ): T = neo4j.write { tx ->
        DefaultMemoryBuilder(tx).run {
            block()
        }
    }

    override suspend fun query(
        cypher: String
    ): Flow<Record> = neo4j.flow(cypher)

    override suspend fun <T> modify(
        cypher: String,
        block: suspend (com.xemantic.neo4j.driver.Result) -> T
    ): T = neo4j.write { tx ->
        val result = tx.run(cypher)
        block(result)
    }

    private inner class DefaultMemoryBuilder(
        private val tx: TransactionContext
    ) : MemoryBuilder {

        override suspend fun node(
            block: suspend NodeBuilder.() -> Unit
        ): Long {
            val builder = DefaultNodeBuilder().apply {
                block()
            }
            builder.validate()
            val nodeLabel = builder.label

            return tx.run(
                query = $$"""
                    MATCH (phenomenon:Fulfillment) WHERE ID(phenomenon) = $fulfillmentId
                    CREATE (n:$$nodeLabel)
                    SET n += $props
                    CREATE (phenomenon)-[:actualizes]->(n)
                    RETURN ID(n) as id
                """.trimIndent(),
                parameters = mapOf(
                    "fulfillmentId" to fulfillmentId,
                    "props" to builder.props
                )
            ).single()["id"].asLong()
        }

        override suspend fun relationship(
            block: suspend RelationshipBuilder.() -> Unit
        ): Long {
            val builder = DefaultRelationshipBuilder().apply {
                block()
            }
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

    val label get() = (listOfNotNull(type) + additionalTypes).joinToString(":")

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
