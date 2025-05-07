/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script

import com.xemantic.ai.golem.server.script.service.DefaultMemory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.harness.Neo4j
import org.neo4j.harness.junit.extension.Neo4jExtension
import kotlin.use

@ExtendWith(Neo4jExtension::class)
class FactRememberingTest {

    @Test
    fun rememberFact(neo4j: Neo4j) = testWithNeo4jDriver(neo4j) { driver ->
        val memory = DefaultMemory(driver)
        memory.remember {
            subject {
                type = "Person"
                properties(
                    "name" to "John Smith"
                )
            }
            predicate = "worksAt"
            target {
                type = "Organization"
                properties(
                    "name" to "Acme"
                )
            }
            properties("foo" to "bar")
            source = "Context[123]/Message[456]"
        }

        memory.query("""
            MATCH ()-[r]->() 
            RETURN type(r) AS type, r AS relationship, id(r) AS id
            LIMIT 100
        """.trimIndent()) {
            val value = it.single()
            val rel = it.single()["r"].asRelationship()
            println(rel)
        }
    }

}

private fun testWithNeo4jDriver(neo4j: Neo4j, block: (driver: Driver) -> Unit) {
    GraphDatabase.driver(neo4j.boltURI(), AuthTokens.none()).use { driver ->
        block(driver)
    }
}
