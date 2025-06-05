/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceInfo
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceMemory
import com.xemantic.ai.golem.api.backend.CulminatedWithIntent
import com.xemantic.ai.golem.api.backend.PhenomenalExpressionInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.neo4j.driver.Driver
import org.neo4j.driver.types.Node

class Neo4jCognitiveWorkspaceMemory(
    private val driver: Driver
) : CognitiveWorkspaceMemory {

    private val logger = KotlinLogging.logger {}

    override suspend fun createWorkspace(
        parentId: Long?
    ): CognitiveWorkspaceInfo {

        logger.debug {
            "Workspace[parentId=$parentId]: creating"
        }

        val workspaceInfo = driver.session().use { session ->

            session.executeWrite { tx ->

                val result = if (parentId != null) {
                    tx.run($$"""
                        MATCH (parent:CognitiveWorkspace) WHERE id(parent) = $parentId
                        CREATE (workspace:CognitiveWorkspace {
                            initiationMoment: datetime()
                        })
                        CREATE (parent)-[:superEvent]->(workspace)
                        CREATE (workspace)-[:subEvent]->(parent)
                        RETURN
                            id(workspace) as id,
                            workspace.initiationMoment as initiationMoment
                    """.trimIndent(), mapOf(
                        "parentId" to parentId
                    ))
                } else {
                    tx.run("""
                        CREATE (workspace:CognitiveWorkspace {
                            initiationMoment: datetime()
                        })
                        RETURN
                            id(workspace) as id,
                            workspace.initiationMoment as initiationMoment
                    """.trimIndent())
                }

                val record = result.single()

                CognitiveWorkspaceInfo(
                    id = record["id"].asLong(),
                    initiationMoment = record["initiationMoment"].asInstant()
                )
            }

        }

        logger.debug {
            "Workspace[${workspaceInfo.id}}]: created"
        }

        return workspaceInfo
    }

    override suspend fun createExpression(
        workspaceId: Long,
        agentId: Long,
    ): PhenomenalExpressionInfo {

        logger.debug {
            "Workspace[$workspaceId]: creating PhenomenalExpression of agentId: $agentId"
        }

        val expressionInfo = driver.session().use { session ->

            session.executeWrite { tx ->

                val result = tx.run($$"""
                    MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                    MATCH (agent:EpistemicAgent) WHERE id(agent) = $agentId
                    CREATE (expression:PhenomenalExpression {
                        initiationMoment: datetime()
                    })
                    CREATE (agent)-[:creator]->(expression)
                    CREATE (workspace)-[:hasPart]->(expression)
                    CREATE (expression)-[:isPartOf]->(workspace)
                    RETURN
                        id(expression) as id,
                        expression.initiationMoment as initiationMoment
                """.trimIndent(), mapOf(
                    "workspaceId" to workspaceId,
                    "agentId" to agentId
                ))

                val record = result.single()

                PhenomenalExpressionInfo(
                    id = record["id"].asLong(),
                    initiationMoment = record["initiationMoment"].asInstant()
                )

            }

        }

        logger.debug {
            "Workspace[$workspaceId]: created PhenomenalExpression[${expressionInfo.id}] of agentId: $agentId"
        }

        return expressionInfo
    }

    override suspend fun createPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        label: String
    ): Long {

        logger.debug {
            "Workspace[$workspaceId]/Expression[$expressionId]: creating Phenomenon[$label]"
        }

        val phenomenonId = driver.session().use { session ->

            session.executeWrite { tx ->

                val result = tx.run($$"""
                    MATCH (expression:PhenomenalExpression) WHERE id(expression) = $expressionId
                    CREATE (phenomenon:Phenomenon:$$label)
                    CREATE (expression)-[:hasPart]->(phenomenon)
                    CREATE (phenomenon)-[:isPartOf]->(expression)
                    RETURN
                        id(phenomenon) as id
                """.trimIndent(), mapOf(
                    "expressionId" to expressionId,
                    "label" to label // TODO most likely label needs to be in the query
                ))

                val record = result.single()

                record["id"].asLong()
            }

        }

        logger.debug {
            "Workspace[$workspaceId]/Expression[$expressionId]/Phenomenon[$phenomenonId]/$label: created"
        }

        return phenomenonId
    }

    override suspend fun getWorkspaceInfo(
        workspaceId: Long
    ): CognitiveWorkspaceInfo {

        logger.debug {
            "Workspace[$workspaceId]: getting CognitiveWorkspaceInfo"
        }

        val workspaceInfo = driver.session().use { session ->

            session.executeRead { tx ->

                val result = tx.run($$"""
                    MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                    RETURN
                        id(workspace) as id,
                        workspace.initiationMoment as initiationMoment
                """.trimIndent(), mapOf(
                    "workspaceId" to workspaceId
                ))

                val record = result.single()

                CognitiveWorkspaceInfo(
                    id = record["id"].asLong(),
                    initiationMoment = record["initiationMoment"].asInstant()
                )
            }

        }

        logger.debug {
            "Workspace[$workspaceId]: retrieved CognitiveWorkspaceInfo"
        }

        return workspaceInfo
    }

    override suspend fun getWorkspaceTitle(
        workspaceId: Long
    ): String? {

        logger.debug {
            "Workspace[$workspaceId]: getting title"
        }

        val title = driver.session().use { session ->

            session.executeRead { tx ->

                val result = tx.run($$"""
                    MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                    RETURN
                        workspace.title as title
                """.trimIndent(), mapOf(
                    "workspaceId" to workspaceId
                ))

                val record = result.single()

                record["title"]?.asString()
            }
        }

        logger.debug {
            "Workspace[$workspaceId]: retrieved title: $title"
        }

        return title
    }

    override suspend fun setWorkspaceTitle(
        workspaceId: Long,
        title: String?
    ) {

        logger.debug {
            "Workspace[$workspaceId]: setting title: $title"
        }

        driver.session().use { session ->

            session.executeWrite { tx ->
                tx.run($$"""
                    MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                    SET workspace.title = $title
                """.trimIndent(), mapOf(
                    "workspaceId" to workspaceId,
                    "title" to title
                ))
            }

        }
    }

    override suspend fun getWorkspaceSummary(
        workspaceId: Long
    ): String? {

        logger.debug {
            "Workspace[$workspaceId]: getting summary"
        }

        val summary = driver.session().use { session ->

            session.executeRead { tx ->

                val result = tx.run($$"""
                    MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                    RETURN
                        workspace.summary as summary
                """.trimIndent(), mapOf(
                    "workspaceId" to workspaceId
                ))

                val record = result.single()

                record["summary"]?.asString()
            }
        }

        logger.debug {
            "Workspace[$workspaceId]: retrieved summary: $summary"
        }

        return summary
    }

    override suspend fun setWorkspaceSummary(
        workspaceId: Long,
        summary: String?
    ) {

        logger.debug {
            "Workspace[$workspaceId]: setting summary: $summary"
        }

        driver.session().use { session ->

            session.executeWrite { tx ->
                tx.run($$"""
                    MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                    SET workspace.summary = $summary
                """.trimIndent(), mapOf(
                    "workspaceId" to workspaceId,
                    "summary" to summary
                ))
            }

        }
    }

    override fun expressions(
        workspaceId: Long
    ): Flow<PhenomenalExpression> = flow {

        logger.debug {
            "Workspace[$workspaceId]: streaming PhenomenalExpressions"
        }

        val list = driver.session().use { session ->
            session.executeRead { tx ->
                val result = tx.run($$"""
                    MATCH (workspace:CognitiveWorkspace)-[:hasPart]->(expression:PhenomenalExpression)
                    MATCH (agent:EpistemicAgent)-[:creator]->(expression)
                    WHERE id(workspace) = $workspaceId
                    
                    OPTIONAL MATCH (expression)-[:hasPart]->(phenomenon:Phenomenon)
                    
                    WITH expression, agent, phenomenon
                    ORDER BY expression.initiationMoment, phenomenon.initiationMoment
                    
                    RETURN
                        id(expression) as expressionId,
                        expression.initiationMoment as initiationMoment,
                        id(agent) as agentId,
                        agent as agent,
                        labels(agent) as agentLabels,
                        collect(phenomenon) as phenomena
                    ORDER BY expression.initiationMoment
                """.trimIndent(), mapOf(
                    "workspaceId" to workspaceId
                ))
                result.stream().map { record ->

                    val epistemicAgent = toEpistemicAgent(
                        id = record["agentId"].asLong(),
                        agent = record["agent"].asNode(),
                        agentLabels = record["agentLabels"].asList {
                            it.asString()
                        }.toSet()
                    )

                    val phenomena = record["phenomena"].asList {
                        val node = it.asNode()
                        val labels = node.labels()
                        logger.trace { "Labels $labels" }
                        when {
                            labels.contains("Text") -> Phenomenon.Text(id = node.id(), text = "")
                            labels.contains("Intent") -> Phenomenon.Intent(id = node.id(), systemId = "", purpose = "", code = "")
                            labels.contains("Fulfilment") -> Phenomenon.Fulfillment(id = node.id(), intentId = "", intentSystemId = "", result = "")
                            else -> throw java.lang.IllegalStateException("Unsupported phenomenon: $labels")
                        }
                    }

                    PhenomenalExpression(
                        id = record["expressionId"].asLong(),
                        agent = epistemicAgent,
                        phenomena = phenomena,
                        initiationMoment = record["initiationMoment"].asInstant()
                    )

                }.toList()
            }
        }
        list.forEach {
            // TODO it should be done much better
            emit(it)
        }
     }

    override suspend fun maybeCulminatedWithIntent(
        workspaceId: Long
    ): CulminatedWithIntent? {

        logger.debug {
            "Workspace[$workspaceId] Checking if culminated with an Intent phenomenon"
        }

        return driver.session().use { session ->
            session.executeRead { tx ->

                val result = tx.run($$"""
                    MATCH (workspace:CognitiveWorkspace)-[:hasPart]->(expression:PhenomenalExpression)-[:hasPart]->(phenomenon:Phenomenon:Intent)
                    WHERE
                        id(workspace) = $workspaceId
                    WITH
                        expression, phenomenon ORDER BY phenomenon.initiationMoment DESC LIMIT 1
                    RETURN
                        id(expression) AS expressionId,
                        id(phenomenon) AS phenomenonId
                """.trimIndent(), mapOf(
                    "workspaceId" to workspaceId
                ))

                if (result.hasNext()) {
                    val record = result.single()
                    CulminatedWithIntent(
                        expressionId = record["expressionId"].asLong(),
                        phenomenonId = record["phenomenonId"].asLong()
                    )
                } else {
                    null
                }
            }
        }
    }

}

private fun toEpistemicAgent(
    id: Long,
    agent: Node,
    agentLabels: Set<String>
): EpistemicAgent = when {
    agentLabels.contains("AI") -> EpistemicAgent.AI(
        id = id,
        model = agent["model"].asString(),
        vendor = agent["vendor"].asString(),
    )
    agentLabels.contains("Human") -> EpistemicAgent.Human(
        id = id
    )
    agentLabels.contains("Computer") -> EpistemicAgent.Computer(
        id = id
    )
    else -> throw IllegalStateException("unsupported agent type: $agentLabels")
}
