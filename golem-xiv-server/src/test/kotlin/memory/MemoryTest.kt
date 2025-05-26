/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.memory

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.harness.Neo4j
import org.neo4j.harness.junit.extension.Neo4jExtension
import kotlin.use

@ExtendWith(Neo4jExtension::class)
class MemoryTest {

    @Test
    fun rememberFact(neo4j: Neo4j) = testWithNeo4jDriver(neo4j) { driver ->
        val memory = DefaultMemory(driver)
        memory.remember {
            val john = node {
                type = "Person"
                properties(
                    "name" to "John Smith"
                )
            }
            val acme = node {
                type = "Organization"
                properties(
                    "name" to "Acme"
                )
            }
            val worksAt = relationship {
                subject = john
                predicate = "worksAt"
                target = acme
                source = "Conversation with John"
                confidence = 1.0
            }
            "john: $john, acme: $acme, worksAt: $worksAt"
        }

        memory.query("""
            MATCH (subject)-[predicate]->(target) 
            RETURN predicate, subject, target
        """.trimIndent()) { result ->
            val record = result.single()

            val relationship = record["predicate"].asRelationship()
            val subject = record["subject"].asNode()
            val target = record["target"].asNode()
        }
    }

}

private fun testWithNeo4jDriver(neo4j: Neo4j, block: (driver: Driver) -> Unit) {
    GraphDatabase.driver(neo4j.boltURI(), AuthTokens.none()).use { driver ->
        block(driver)
    }
}
