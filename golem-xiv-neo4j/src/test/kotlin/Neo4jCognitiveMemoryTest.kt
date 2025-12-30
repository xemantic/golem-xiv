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

}
