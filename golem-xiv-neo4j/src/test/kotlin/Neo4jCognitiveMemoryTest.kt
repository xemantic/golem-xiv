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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class Neo4jCognitiveMemoryTest {

    @AfterEach
    fun cleanDatabase() {
        TestNeo4j.cleanDatabase()
    }

    @Test
    fun `should create cognition`() = runTest {
        // given
        val memory = Neo4jCognitiveMemory(
            neo4j = TestNeo4j.operations
        )
        val constitution = listOf("This is your system prompt")

        // when
        val cognitionInfo = memory.createCognition(
            constitution = constitution
        )

        // then
        cognitionInfo should {
            have(id >= 0)
            have(parentId == null)
        }

        // verify the cognition was created in the database with correct constitution
        val storedCognition = TestNeo4j.operations.read { tx ->
            tx.run(
                $$"""
                MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                RETURN cognition.constitution AS constitution
                """.trimIndent(),
                mapOf("cognitionId" to cognitionInfo.id)
            ).single()
        }

        storedCognition should {
            have(get("constitution").asList { it.asString() } == constitution)
        }
    }

    @Test
    fun `should create cognition with parent`() = runTest {
        // given
        val memory = Neo4jCognitiveMemory(
            neo4j = TestNeo4j.operations
        )
        val parentConstitution = listOf("Parent system prompt")
        val childConstitution = listOf("Child system prompt")

        // when
        val parentInfo = memory.createCognition(
            constitution = parentConstitution
        )
        val childInfo = memory.createCognition(
            constitution = childConstitution,
            parentId = parentInfo.id
        )

        // then
        childInfo should {
            have(id >= 0)
            have(id != parentInfo.id)
            have(parentId == parentInfo.id)
        }

        // verify the parent-child relationship was created in the database
        val relationship = TestNeo4j.operations.read { tx ->
            tx.run(
                $$"""
                MATCH (parent:Cognition)-[:hasChild]->(child:Cognition)
                WHERE id(parent) = $parentId AND id(child) = $childId
                RETURN
                    parent.constitution AS parentConstitution,
                    child.constitution AS childConstitution
                """.trimIndent(),
                mapOf(
                    "parentId" to parentInfo.id,
                    "childId" to childInfo.id
                )
            ).single()
        }

        relationship should {
            have(get("parentConstitution").asList { it.asString() } == parentConstitution)
            have(get("childConstitution").asList { it.asString() } == childConstitution)
        }
    }

    @Test
    fun `should list cognitions ordered by initiation moment descending`() = runTest {
        // given
        val memory = Neo4jCognitiveMemory(
            neo4j = TestNeo4j.operations
        )

        val cognition1 = memory.createCognition(constitution = listOf("First"))
        val cognition2 = memory.createCognition(constitution = listOf("Second"))
        val cognition3 = memory.createCognition(constitution = listOf("Third"))

        // when
        val cognitions = memory.listCognitions()

        // then
        cognitions should {
            have(size == 3)
            have(get(0).id == cognition3.id) // most recent first
            have(get(1).id == cognition2.id)
            have(get(2).id == cognition1.id)
        }
    }

    @Test
    fun `should list cognitions excluding child cognitions`() = runTest {
        // given
        val memory = Neo4jCognitiveMemory(
            neo4j = TestNeo4j.operations
        )

        val parentCognition = memory.createCognition(constitution = listOf("Parent"))
        val childCognition = memory.createCognition(
            constitution = listOf("Child"),
            parentId = parentCognition.id
        )
        val standaloneCognition = memory.createCognition(constitution = listOf("Standalone"))

        // when
        val cognitions = memory.listCognitions()

        // then
        cognitions should {
            have(size == 2) // only parent and standalone, not child
            have(any { it.id == parentCognition.id })
            have(any { it.id == standaloneCognition.id })
            have(none { it.id == childCognition.id })
        }
    }

    @Test
    fun `should list cognitions with title`() = runTest {
        // given
        val memory = Neo4jCognitiveMemory(
            neo4j = TestNeo4j.operations
        )

        val cognition = memory.createCognition(constitution = listOf("Test"))
        memory.setCognitionTitle(cognition.id, "My Cognition Title")

        // when
        val cognitions = memory.listCognitions()

        // then
        cognitions should {
            have(size == 1)
            have(get(0).title == "My Cognition Title")
        }
    }

    @Test
    fun `should list cognitions with limit and offset`() = runTest {
        // given
        val memory = Neo4jCognitiveMemory(
            neo4j = TestNeo4j.operations
        )

        repeat(5) {
            memory.createCognition(constitution = listOf("Cognition $it"))
        }

        // when
        val firstPage = memory.listCognitions(limit = 2, offset = 0)
        val secondPage = memory.listCognitions(limit = 2, offset = 2)

        // then
        firstPage should { have(size == 2) }
        secondPage should { have(size == 2) }
        firstPage.map { it.id } should { have(none { id -> secondPage.any { it.id == id } }) }
    }

    @Test
    fun `should return expression count of zero for new cognition`() = runTest {
        // given
        val memory = Neo4jCognitiveMemory(
            neo4j = TestNeo4j.operations
        )
        val cognition = memory.createCognition(constitution = listOf("Test"))

        // when
        val count = memory.getExpressionCount(cognition.id)

        // then
        count should { have(this == 0) }
    }

    @Test
    fun `should return correct expression count`() = runTest {
        // given
        val memory = Neo4jCognitiveMemory(
            neo4j = TestNeo4j.operations
        )
        val cognition = memory.createCognition(constitution = listOf("Test"))

        // create an agent and expressions directly via Cypher
        val cognitionId = cognition.id
        TestNeo4j.operations.write { tx ->
            tx.run(
                """
                CREATE (agent:EpistemicAgent:Human)
                WITH agent
                MATCH (c:Cognition) WHERE id(c) = ${'$'}cognitionId
                CREATE (e1:PhenomenalExpression {initiationMoment: datetime()})
                CREATE (e2:PhenomenalExpression {initiationMoment: datetime()})
                CREATE (e3:PhenomenalExpression {initiationMoment: datetime()})
                CREATE (agent)-[:creator]->(e1)
                CREATE (agent)-[:creator]->(e2)
                CREATE (agent)-[:creator]->(e3)
                CREATE (c)-[:hasPart]->(e1)
                CREATE (c)-[:hasPart]->(e2)
                CREATE (c)-[:hasPart]->(e3)
                """.trimIndent(),
                mapOf("cognitionId" to cognitionId)
            )
        }

        // when
        val count = memory.getExpressionCount(cognition.id)

        // then
        count should { have(this == 3) }
    }

}
