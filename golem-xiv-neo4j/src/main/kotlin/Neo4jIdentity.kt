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

    override val selfId: Long by lazy {

        val id = driver.session().use { session ->

            val maybeId = session.executeRead { tx ->

                val result = tx.run("""
                    MATCH (self:Self:Agent:AI)
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
                        CREATE (self:Self:Agent:AI {
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

        id
    }

    override suspend fun userId(
        login: String
    ): Long = -1 // TODO we need to think it through

}
