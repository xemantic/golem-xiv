/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script.service

import com.xemantic.ai.golem.server.script.FactBuilder
import com.xemantic.ai.golem.server.script.Memory
import com.xemantic.ai.golem.server.script.NodeBuilder
import org.neo4j.driver.Driver
import org.neo4j.driver.Result
import org.neo4j.driver.TransactionContext
import org.neo4j.driver.Values
import org.neo4j.driver.types.Node
import kotlin.uuid.Uuid

class DefaultMemory(
    private val driver: Driver
) : Memory {

    override fun remember(
        block: FactBuilder.() -> Unit
    ) {
        val builder = DefaultFactBuilder().also(block)
        builder.validate()

        driver.session().use { session ->
            session.executeWrite { tx ->

//                val subject = builder.subjectBuilder.storeNode(tx)
//                val target = builder.targetBuilder.storeNode(tx)
                val subjectLabel = builder.subjectBuilder.label
                val targetLabel = builder.targetBuilder.label
                val subjectId = Uuid.random().toString()
                val targetId = Uuid.random().toString()

                println("Subject ID: $subjectId")
                println("Target ID: $targetId")
                println("Predicate: ${builder.predicate}")
                println("Properties: ${builder.props}")

                val relationshipQuery = $$"""
                    CREATE (s$$subjectLabel {externalId: $subjectId})
                    SET s += $subjectProps
                    CREATE (t$$targetLabel {externalId: $targetId})
                    SET t += $targetProps

                    WITH s, t
                    MATCH (s$$subjectLabel {externalId: $subjectId})
                    MATCH (t$$targetLabel {externalId: $targetId})
                    CREATE (s)-[r:$${builder.predicate} $props]->(t)
                    SET r += $props
                    RETURN ID(r) as relationshipId
                """.trimIndent()

                val result = tx.run(
                    relationshipQuery,
                    Values.parameters(
                        "externalId", Uuid.random().toString(),
                        "subjectId", subjectId,
                        "subjectProps", builder.subjectBuilder.props,
                        "targetId", targetId,
                        "targetProps", builder.targetBuilder.props,
                        // TODO fix time
                        //"timestamp", Clock.System.now(),
                        "props", builder.props
                    )
                )

                val record = result.single()
                val generatedId = record["relationshipId"].asLong()
                println("Generated relationship ID: $generatedId")
                generatedId
            }
        }
    }

    override fun <T> query(
        cypher: String,
        block: (Result) -> T
    ): T  = driver.session().use { session ->
        session.executeRead { tx ->
            val result = tx.run(cypher)
            block(result)
        }
    }

}

private class DefaultFactBuilder : FactBuilder {

    override var predicate: String? = null
    override var source: String? = null
    override var confidence: Double = 1.0

    lateinit var subjectBuilder: DefaultNodeBuilder
    lateinit var targetBuilder: DefaultNodeBuilder
    val props: MutableMap<String, Any> = mutableMapOf()

    override fun subject(block: NodeBuilder.() -> Unit) {
        subjectBuilder = DefaultNodeBuilder().also(block)
    }

    override fun target(block: NodeBuilder.() -> Unit) {
        targetBuilder = DefaultNodeBuilder().also(block)
    }

    override fun properties(vararg props: Pair<String, Any>) {
        this.props += props.toMap()
    }

    fun validate() {
        requireNotNull(predicate) { "predicate must be set" }
        requireNotNull(source) { "source must be set" }
        require(::subjectBuilder.isInitialized) { "subject you must provider" }
        require(::targetBuilder.isInitialized) { "target you must provider" }
        subjectBuilder.validate("subject")
        targetBuilder.validate("target")
    }

}

private class DefaultNodeBuilder() : NodeBuilder {

    override var type: String? = null
    override val additionalTypes: MutableList<String> = mutableListOf()

    val props: MutableMap<String, Any> = mutableMapOf()

    override fun properties(vararg props: Pair<String, Any>) {
        this.props += props.toMap()
    }

    fun validate(nodeRole: String) {
        requireNotNull(type) {
            "${nodeRole}: type must be specified"
        }
        requireNotNull(props)
    }

    fun storeNode(tx: TransactionContext): Node {
        val id = Uuid.random().toString()
        val allTypes = listOfNotNull(type) + additionalTypes
        val labelString = allTypes.joinToString(":", prefix = ":")

        val query = $$"""
            CREATE (n$$labelString {id: $id})
            SET n += $props
            RETURN n
        """.trimIndent()

        val parameters = mapOf(
            "id" to id,
            "props" to props
        )

        val result = tx.run(query, parameters)
        val record = result.single()

        return record["n"].asNode()
    }

    val label get() = (listOfNotNull(type) + additionalTypes).joinToString(":", prefix = ":")

}
