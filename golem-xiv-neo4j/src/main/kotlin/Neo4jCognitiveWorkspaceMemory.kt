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
                    tx.runCypher(query =
                        $$"""
                            MATCH (parent:CognitiveWorkspace) WHERE id(parent) = $parentId
                            CREATE (workspace:CognitiveWorkspace {
                                title: "Untitled",
                                summary: "",
                                initiationMoment: datetime()
                            })
                            CREATE (parent)-[:superEvent]->(workspace)
                            CREATE (workspace)-[:subEvent]->(parent)
                            RETURN
                                id(workspace) as id,
                                workspace.initiationMoment as initiationMoment
                        """.trimIndent(),
                        parameters = mapOf(
                            "parentId" to parentId
                        )
                    )
                } else {
                    tx.runCypher(query = """
                        CREATE (workspace:CognitiveWorkspace {
                            initiationMoment: datetime()
                        })
                        RETURN
                            id(workspace) as id,
                            workspace.initiationMoment as initiationMoment
                    """.trimIndent()
                    )
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
                val result = tx.runCypher(
                    query = $$"""
                        MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                        MATCH (agent:EpistemicAgent) WHERE id(agent) = $agentId
                        CREATE (expression:PhenomenalExpression {
                            title: "Expression",
                            initiationMoment: datetime()
                        })
                        SET expression.title = expression.title + " " + id(expression)
                        CREATE (agent)-[:creator]->(expression)
                        CREATE (workspace)-[:hasPart]->(expression)
                        CREATE (expression)-[:isPartOf]->(workspace)
                        RETURN
                            id(expression) as id,
                            expression.initiationMoment as initiationMoment
                    """.trimIndent(),
                    parameters = mapOf(
                        "workspaceId" to workspaceId,
                        "agentId" to agentId
                    )
                )

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
            "Cognition[$workspaceId]/Expression[$expressionId]: creating Phenomenon ($label)"
        }

        val phenomenonId = driver.session().use { session ->

            session.executeWrite { tx ->

                val result = tx.runCypher(
                    query = $$"""
                        MATCH (expression:PhenomenalExpression) WHERE id(expression) = $expressionId
                        CREATE (phenomenon:Phenomenon:$$label)
                        SET phenomenon.title = "$$label " + id(phenomenon)
                        CREATE (expression)-[:hasPart]->(phenomenon)
                        CREATE (phenomenon)-[:isPartOf]->(expression)
                        RETURN
                            id(phenomenon) as id
                    """.trimIndent(),
                    parameters = mapOf(
                        "expressionId" to expressionId
                    )
                )

                val record = result.single()

                record["id"].asLong()
            }

        }

        logger.debug {
            "Cognition[$workspaceId]/Expression[$expressionId]/Phenomenon[$phenomenonId]($label): created"
        }

        return phenomenonId
    }

    override suspend fun createFulfillmentPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        intentId: Long
    ): Long {

        logger.debug {
            "Cognition[$workspaceId]/Expression[$expressionId]: creating Phenomenon(Fulfillment)"
        }

        val phenomenonId = driver.session().use { session ->

            session.executeWrite { tx ->

                val result = tx.runCypher(
                    query = $$"""
                        MATCH (expression:PhenomenalExpression) WHERE id(expression) = $expressionId
                        CREATE (fulfillment:Phenomenon:Fulfillment)
                        SET fulfillment.title = "Fulfillment " + id(fulfillment)
                        CREATE (expression)-[:hasPart]->(fulfillment)
                        CREATE (fulfillment)-[:isPartOf]->(expression)
                        
                        WITH fulfillment
                        MATCH (intent:Phenomenon:Intent) WHERE id(intent) = $intentId
                        CREATE (fulfillment)-[:fulfills]->(intent)

                        RETURN
                            id(fulfillment) as id
                    """.trimIndent(),
                    parameters = mapOf(
                        "expressionId" to expressionId,
                        "intentId" to intentId
                    )
                )

                val record = result.single()

                record["id"].asLong()
            }

        }

        logger.debug {
            "Cognition[$workspaceId]/Expression[$expressionId]/Phenomenon[$phenomenonId](Fulfillment): created"
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

                val result = tx.runCypher(query = $$"""
                    MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                    RETURN
                        id(workspace) as id,
                        workspace.initiationMoment as initiationMoment
                """.trimIndent(),
                    parameters = mapOf(
                        "workspaceId" to workspaceId
                    )
                )

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

                val result = tx.runCypher(query = $$"""
                    MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                    RETURN
                        workspace.title as title
                    """.trimIndent(),
                    parameters = mapOf(
                        "workspaceId" to workspaceId
                    )
                )

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
                tx.runCypher(
                    query = $$"""
                        MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                        SET workspace.title = $title
                    """.trimIndent(),
                    parameters = mapOf(
                        "workspaceId" to workspaceId,
                        "title" to title
                    )
                )
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

                val result = tx.runCypher(
                    query = $$"""
                        MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                        RETURN
                            workspace.summary as summary
                    """.trimIndent(), mapOf(
                        "workspaceId" to workspaceId
                    )
                )

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
                tx.runCypher(
                    query = $$"""
                        MATCH (workspace:CognitiveWorkspace) WHERE id(workspace) = $workspaceId
                        SET workspace.summary = $summary
                    """.trimIndent(), mapOf(
                        "workspaceId" to workspaceId,
                        "summary" to summary
                    )
                )
            }

        }
    }

    override fun expressions(
        cognitionId: Long
    ): Flow<PhenomenalExpression> = flow {

        logger.debug {
            "Cognition[$cognitionId]: flowing PhenomenalExpressions"
        }

        val list = driver.session().use { session ->
            session.executeRead { tx ->
                val result = tx.runCypher(
                    query = $$"""
                        MATCH (workspace:CognitiveWorkspace)-[:hasPart]->(expression:PhenomenalExpression)
                        MATCH (agent:EpistemicAgent)-[:creator]->(expression)
                        WHERE id(workspace) = $cognitionId
                        
                        OPTIONAL MATCH (expression)-[:hasPart]->(phenomenon:Phenomenon)
                        OPTIONAL MATCH (phenomenon)-[:fulfills]->(intent:Phenomenon:Intent)
                        
                        WITH expression, agent, phenomenon, intent
                        ORDER BY id(expression), id(phenomenon)
                        
                        RETURN
                            id(expression) as expressionId,
                            expression.initiationMoment as initiationMoment,
                            id(agent) as agentId,
                            agent as agent,
                            labels(agent) as agentLabels,
                            collect({
                                phenomenon: phenomenon,
                                intent: intent
                            }) as phenomenaWithIntents
                        ORDER BY id(expression)
                    """.trimIndent(),
                    parameters = mapOf(
                        "cognitionId" to cognitionId
                    )
                )
                result.stream().map { record ->

                    val epistemicAgent = toEpistemicAgent(
                        id = record["agentId"].asLong(),
                        agent = record["agent"].asNode(),
                        agentLabels = record["agentLabels"].asList {
                            it.asString()
                        }.toSet()
                    )

                    val phenomena = record["phenomenaWithIntents"].asList {
                        val itemMap = it.asMap()
                        val phenomenonValue = itemMap["phenomenon"]
                        val node = phenomenonValue as Node
                        val labels = node.labels()
                        when {
                            labels.contains("Text") -> Phenomenon.Text(
                                id = node.id(),
                                text = "" // will be filled from storage
                            )
                            labels.contains("Intent") -> Phenomenon.Intent(
                                id = node.id(),
                                systemId = "",
                                purpose = "",
                                code = ""
                            )// will be filled from storage
                            labels.contains("Fulfillment") -> Phenomenon.Fulfillment(
                                id = node.id(),
                                intentId = (itemMap["intent"] as Node).id(),
                                intentSystemId = "",
                                result = ""
                            )
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

                // TODO for sure there is easier way of doing it
                val result = tx.runCypher(
                    query = $$"""
                        MATCH (workspace:CognitiveWorkspace)-[:hasPart]->(expression:PhenomenalExpression)
                        WHERE id(workspace) = $workspaceId
                        
                        WITH MAX(id(expression)) as maxExpressionId
                        
                        MATCH (workspace:CognitiveWorkspace)-[:hasPart]->(expression:PhenomenalExpression)-[:hasPart]->(phenomenon:Phenomenon)
                        WHERE id(workspace) = $workspaceId AND id(expression) = maxExpressionId
                        
                        WITH expression, MAX(id(phenomenon)) as maxPhenomenonId
                        
                        OPTIONAL MATCH (expression)-[:hasPart]->(maxPhenomenon:Phenomenon:Intent)
                        WHERE id(maxPhenomenon) = maxPhenomenonId
                        
                        RETURN
                            id(expression) AS expressionId,
                            id(maxPhenomenon) AS phenomenonId
                    """.trimIndent(),
                    parameters = mapOf(
                        "workspaceId" to workspaceId
                    )
                )

                if (result.hasNext()) {
                    val record = result.single()
                    val idValue = record["phenomenonId"]
                    if (idValue.isNull) {
                        null
                    } else {
                        CulminatedWithIntent(
                            expressionId = record["expressionId"].asLong(),
                            phenomenonId = idValue.asLong()
                        )
                    }
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
