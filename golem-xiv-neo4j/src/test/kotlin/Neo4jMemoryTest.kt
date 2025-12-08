/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class Neo4jMemoryTest {

    @AfterEach
    fun cleanDatabase() {
        TestNeo4j.cleanDatabase()
    }

    @Test
    fun `should remember fact`() = runTest {
        // given
        TestNeo4j.populate("""
            CREATE (cognition:Cognition {
                title: 'Test Cognition',
                summary: '',
                constitution: ['test constitution'],
                initiationMoment: datetime()
            })
            CREATE (agent:EpistemicAgent:AI {
                model: 'test-model',
                vendor: 'test-vendor'
            })
            CREATE (expression:PhenomenalExpression {
                title: 'Expression 1',
                initiationMoment: datetime()
            })
            CREATE (intent:Phenomenon:Intent {
                title: 'Intent 1'
            })
            CREATE (fulfillment:Phenomenon:Fulfillment {
                title: 'Fulfillment 1'
            })
            CREATE (agent)-[:creator]->(expression)
            CREATE (cognition)-[:hasPart]->(expression)
            CREATE (expression)-[:hasPart]->(intent)
            CREATE (expression)-[:hasPart]->(fulfillment)
            CREATE (fulfillment)-[:fulfills]->(intent)
        """.trimIndent())

        val (cognitionId, fulfillmentId) = TestNeo4j.operations.read { tx ->
            tx.run("""
                MATCH (cognition:Cognition)
                MATCH (fulfillment:Phenomenon:Fulfillment)
                RETURN id(cognition) AS cognitionId, id(fulfillment) AS fulfillmentId
            """.trimIndent()).single().let { record ->
                record["cognitionId"].asLong() to record["fulfillmentId"].asLong()
            }
        }

        val memory = Neo4jMemory(
            neo4j = TestNeo4j.operations,
            cognitionId = cognitionId,
            fulfillmentId = fulfillmentId
        )

        // when
        val output = memory.remember {
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
            Triple(john, worksAt, acme)
        }

        // then
        // verify remember returns valid node and relationship IDs
        output should {
            have(first > 0) // john node ID
            have(second > 0) // worksAt relationship ID
            have(third > 0) // acme node ID
        }

        // verify nodes are linked to fulfillment via :actualizes relationship
        val actualizedNodes = TestNeo4j.operations.flow("""
            MATCH (fulfillment:Fulfillment)-[:actualizes]->(n)
            RETURN labels(n) AS labels, n.name AS name
            ORDER BY name
        """.trimIndent()).toList()

        actualizedNodes should {
            have(size == 2)
            this[0] should {
                have(get("labels").asList { it.asString() } == listOf("Organization"))
                have(get("name").asString() == "Acme")
            }
            this[1] should {
                have(get("labels").asList { it.asString() } == listOf("Person"))
                have(get("name").asString() == "John Smith")
            }
        }

        // verify the worksAt relationship was created
        val relationships = TestNeo4j.operations.flow("""
            MATCH (person:Person)-[r:worksAt]->(org:Organization)
            RETURN person.name AS personName, org.name AS orgName
        """.trimIndent()).toList()

        relationships should {
            have(size == 1)
            this[0] should {
                have(get("personName").asString() == "John Smith")
                have(get("orgName").asString() == "Acme")
            }
        }

    }

}
