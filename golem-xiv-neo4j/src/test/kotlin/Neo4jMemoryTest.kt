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
            Triple(john, worksAt, acme)
        }

        // then
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
