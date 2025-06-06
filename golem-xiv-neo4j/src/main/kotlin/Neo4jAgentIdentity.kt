/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.backend.AgentIdentity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.neo4j.driver.Driver

class Neo4jAgentIdentity(
    private val driver: Driver
) : AgentIdentity {

    private val logger = KotlinLogging.logger {}

    // TODO this should be memorized
    override suspend fun getSelfId(): Long {

        logger.debug {
            "AgentIdentity: getting self id"
        }

        val id = driver.session().use { session ->

            val maybeId = session.executeRead { tx ->

                val result = tx.run("""
                    MATCH (self:EpistemicAgent:AI:Self)
                    RETURN id(self) as id
                """.trimIndent())

                if (result.hasNext()) {
                    result.single()["id"].asLong()
                } else {
                    null


                }
            }

            if (maybeId != null) {
                maybeId
            } else {

                logger.info {
                    "No self-identity node detected, creating"
                }

                session.executeWrite { tx ->
                    val result = tx.run("""
                        CREATE (self:EpistemicAgent:AI:Self {
                            initiationMoment: datetime()
                        })
                        RETURN id(self) as id
                    """.trimIndent())
                    result.single()["id"].asLong()
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

        val id = driver.session().use { session ->

            val maybeId = session.executeRead { tx ->

                val result = tx.run("""
                    MATCH (human:EpistemicAgent:Human)
                    RETURN id(human) as id
                """.trimIndent())

                if (result.hasNext()) {
                    result.single()["id"].asLong()
                } else {
                    null


                }
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

        val id = driver.session().use { session ->

            val maybeId = session.executeRead { tx ->

                val result = tx.run("""
                    MATCH (computer:EpistemicAgent:Computer)
                    RETURN id(computer) as id
                """.trimIndent())

                if (result.hasNext()) {
                    result.single()["id"].asLong()
                } else {
                    null


                }
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
