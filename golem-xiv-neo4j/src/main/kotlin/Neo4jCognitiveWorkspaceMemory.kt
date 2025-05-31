/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceInfo
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceMemory
import com.xemantic.ai.golem.api.backend.PhenomenalExpressionInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.neo4j.driver.Driver
import org.neo4j.driver.Value
import kotlin.time.toKotlinInstant

class Neo4jCognitiveWorkspaceMemory(
    private val driver: Driver
) : CognitiveWorkspaceMemory {

    private val logger = KotlinLogging.logger {}

    override suspend fun createWorkspace(
        parentId: Long?
    ): CognitiveWorkspaceInfo {

        logger.debug { "Creating workspace, parent: $parentId" }

        return driver.session().use { session ->

            val writeResult = session.executeWrite { tx ->
                tx.run("""
                    CREATE (workspace:CognitiveWorkspace {
                        initiationMoment: datetime()
                    })
                    RETURN workspace.id as id, workspace.initiationMoment as initiationMoment
                """.trimIndent())
            }

            val result = writeResult.single()
            CognitiveWorkspaceInfo(
                id = result["id"].asLong(),
                initiationMoment = result["initiationMoment"].asInstant()
            )
        }
    }

    override suspend fun createExpression(
        workspaceId: Long,
        agentId: Long
    ): PhenomenalExpressionInfo {

        logger.debug { "Creating expression, workspaceId: $workspaceId, agentId: $agentId" }

        return driver.session().use { session ->

            val writeResult = session.executeWrite { tx ->
                tx.run("""
                    CREATE (expression:PhenomenalExpression {
                        initiationMoment: datetime()
                    })
                    RETURN expression.id as id, expression.initiationMoment as initiationMoment
                """.trimIndent())
            }

            val result = writeResult.single()
            PhenomenalExpressionInfo(
                id = result["id"].asLong(),
                initiationMoment = result["initiationMoment"].asInstant()
            )
        }
    }

    override suspend fun createPhenomenon(
        expressionId: Long
    ): Long {

        logger.debug { "Creating phenomenon, expressionId: $expressionId" }

        return driver.session().use { session ->

            val writeResult = session.executeWrite { tx ->
                tx.run("""
                    CREATE (phenomenon:Phenomenon)
                    RETURN phenomenon.id as id
                """.trimIndent())
            }

            val result = writeResult.single()
            result["id"].asLong()
        }
    }

    override suspend fun updateWorkspace(workspaceId: Long, title: String?, summary: String?) {
        TODO("Not yet implemented")
    }

}

// TODO move to neo4j common
fun Value.asInstant() = asZonedDateTime().toInstant().toKotlinInstant()
