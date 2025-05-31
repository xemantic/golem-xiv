/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import com.xemantic.ai.golem.api.backend.Identity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.neo4j.driver.Driver

class Neo4jIdentity(
    private val driver: Driver
) : Identity {

    private val logger = KotlinLogging.logger {}

    override fun selfId(): Long {

        val id = driver.session().use { session ->

            val result = session.executeRead { tx ->
                tx.run("""
                    MATCH (self:Self:Agent:AI)
                    RETURN self.id as id
                """.trimIndent())
            }

            if (result.hasNext()) {
                result.single()["id"].asLong()
            } else {

                logger.info {
                    "No self-identity node detected, creating"
                }

                val writeResult = session.executeWrite { tx ->
                    tx.run("""
                        CREATE (self:Self:Agent:AI) {
                            initiationMoment: datetime()
                        })
                        RETURN self.id as id
                    """.trimIndent())
                }

                writeResult.single()["id"].asLong()
            }
        }

        logger.info {
            "Self-identity node id: $id"
        }

        return id
    }

    override fun userId(
        login: String
    ): Long {
        TODO()
    }

}