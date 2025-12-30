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

import com.xemantic.ai.golem.api.backend.AgentIdentity
import com.xemantic.neo4j.driver.Neo4jOperations
import com.xemantic.neo4j.driver.singleOrNull
import io.github.oshai.kotlinlogging.KotlinLogging

class Neo4jAgentIdentity(
    private val neo4j: Neo4jOperations
) : AgentIdentity {

    private val logger = KotlinLogging.logger {}

    // TODO this should be memorized
    override suspend fun getSelfId(): Long {

        logger.debug {
            "AgentIdentity: getting self id"
        }

        val id = neo4j.withSession { session ->

            val maybeId = session.executeRead { tx ->
                tx.run("""
                    MATCH (self:EpistemicAgent:AI:Self)
                    RETURN id(self) as id
                """.trimIndent()).singleOrNull()?.get("id")?.asLong()
            }

            if (maybeId != null) {
                maybeId
            } else {

                logger.info {
                    "No self-identity node detected, creating"
                }

                session.executeWrite { tx ->
                    tx.run("""
                        CREATE (self:EpistemicAgent:AI:Self {
                            initiationMoment: datetime()
                        })
                        RETURN id(self) as id
                    """.trimIndent()).single()["id"].asLong()
                }

            }

        }

        logger.info {
            "Self-identity node id: $id"
        }

        return id
    }


    override suspend fun getUserId(login: String): Long {

        logger.debug {
            "AgentIdentity: getting user id"
        }

        val id = neo4j.withSession { session ->

            val maybeId = session.executeRead { tx ->
                tx.run("""
                    MATCH (human:EpistemicAgent:Human)
                    RETURN id(human) as id
                """.trimIndent()).singleOrNull()?.get("id")?.asLong()
            }

            if (maybeId != null) {
                maybeId
            } else {

                logger.info {
                    "No human-identity node detected, creating"
                }

                session.executeWrite { tx ->
                    val result = tx.run("""
                        CREATE (human:EpistemicAgent:Human {
                            initiationMoment: datetime()
                        })
                        RETURN id(human) as id
                    """.trimIndent())
                    result.single()["id"].asLong()
                }

            }

        }

        logger.info {
            "Human-identity node id: $id"
        }

        return id
    }

    override suspend fun getComputerId(): Long {

        logger.debug {
            "AgentIdentity: getting computer id"
        }

        val id = neo4j.withSession { session ->

            val maybeId = session.executeRead { tx ->
                tx.run("""
                    MATCH (computer:EpistemicAgent:Computer)
                    RETURN id(computer) as id
                """.trimIndent()).singleOrNull()?.get("id")?.asLong()
            }

            if (maybeId != null) {
                maybeId
            } else {

                logger.info {
                    "No computer-identity node detected, creating"
                }

                session.executeWrite { tx ->
                    val result = tx.run("""
                        CREATE (computer:EpistemicAgent:Computer {
                            initiationMoment: datetime()
                        })
                        RETURN id(computer) as id
                    """.trimIndent())
                    result.single()["id"].asLong()
                }

            }

        }

        logger.info {
            "Computer-identity node id: $id"
        }

        return id
    }

}
