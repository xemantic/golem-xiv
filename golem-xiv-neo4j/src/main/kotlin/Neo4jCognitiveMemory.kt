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
import com.xemantic.ai.golem.api.backend.CognitionInfo
import com.xemantic.ai.golem.api.backend.CognitiveMemory
import com.xemantic.ai.golem.api.backend.CulminatedWithIntent
import com.xemantic.ai.golem.api.backend.PhenomenalExpressionInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.neo4j.driver.Driver
import org.neo4j.driver.types.Node

class Neo4jCognitiveMemory(
    private val driver: Driver
) : CognitiveMemory {

    private val logger = KotlinLogging.logger {}

    override suspend fun createCognition(
        parentId: Long?
    ): CognitionInfo {

        logger.debug {
            "Cognition[parentId=$parentId]: creating"
        }

        val cognitionInfo = driver.session().use { session ->

            session.executeWrite { tx ->

                val result = if (parentId != null) {
                    tx.runCypher(
                        query = $$"""
                            MATCH (parent:Cognition) WHERE id(parent) = $parentId
                            CREATE (cognition:Cognition {
                                title: "Untitled",
                                summary: "",
                                initiationMoment: datetime()
                            })
                            CREATE (parent)-[:hasChild]->(cognition)
                            RETURN
                                id(cognition) as id,
                                cognition.initiationMoment as initiationMoment
                        """.trimIndent(),
                        parameters = mapOf(
                            "parentId" to parentId
                        )
                    )
                } else {
                    tx.runCypher(query = """
                        CREATE (cognition:Cognition {
                            initiationMoment: datetime()
                        })
                        RETURN
                            id(cognition) as id,
                            cognition.initiationMoment as initiationMoment
                    """.trimIndent()
                    )
                }

                val record = result.single()

                CognitionInfo(
                    id = record["id"].asLong(),
                    parentId = parentId,
                    initiationMoment = record["initiationMoment"].asInstant(),
                )
            }

        }

        logger.debug {
            "Cognition[${cognitionInfo.id}}]: created"
        }

        return cognitionInfo
    }

    override suspend fun createExpression(
        cognitionId: Long,
        agentId: Long,
    ): PhenomenalExpressionInfo {

        logger.debug {
            "Cognition[$cognitionId]: creating PhenomenalExpression of agentId: $agentId"
        }

        val expressionInfo = driver.session().use { session ->

            session.executeWrite { tx ->
                val result = tx.runCypher(
                    query = $$"""
                        MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                        MATCH (agent:EpistemicAgent) WHERE id(agent) = $agentId
                        CREATE (expression:PhenomenalExpression {
                            title: "Expression",
                            initiationMoment: datetime()
                        })
                        SET expression.title = expression.title + " " + id(expression)
                        CREATE (agent)-[:creator]->(expression)
                        CREATE (cognition)-[:hasPart]->(expression)
                        RETURN
                            id(expression) as id,
                            expression.initiationMoment as initiationMoment
                    """.trimIndent(),
                    parameters = mapOf(
                        "cognitionId" to cognitionId,
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
            "Cognition[$cognitionId]: created PhenomenalExpression[${expressionInfo.id}] of agentId: $agentId"
        }

        return expressionInfo
    }

    override suspend fun createPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        label: String
    ): Long {

        logger.debug {
            "Cognition[$cognitionId]/Expression[$expressionId]: creating Phenomenon ($label)"
        }

        val phenomenonId = driver.session().use { session ->

            session.executeWrite { tx ->

                val result = tx.runCypher(
                    query = $$"""
                        MATCH (expression:PhenomenalExpression) WHERE id(expression) = $expressionId
                        CREATE (phenomenon:Phenomenon:$$label)
                        SET phenomenon.title = "$$label " + id(phenomenon)
                        CREATE (expression)-[:hasPart]->(phenomenon)
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
            "Cognition[$cognitionId]/Expression[$expressionId]/Phenomenon[$phenomenonId]($label): created"
        }

        return phenomenonId
    }

    override suspend fun createFulfillmentPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        intentId: Long
    ): Long {

        logger.debug {
            "Cognition[$cognitionId]/Expression[$expressionId]: creating Phenomenon(Fulfillment)"
        }

        val phenomenonId = driver.session().use { session ->

            session.executeWrite { tx ->

                val result = tx.runCypher(
                    query = $$"""
                        MATCH (expression:PhenomenalExpression) WHERE id(expression) = $expressionId
                        CREATE (fulfillment:Phenomenon:Fulfillment)
                        SET fulfillment.title = "Fulfillment " + id(fulfillment)
                        CREATE (expression)-[:hasPart]->(fulfillment)
                        
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
            "Cognition[$cognitionId]/Expression[$expressionId]/Phenomenon[$phenomenonId](Fulfillment): created"
        }

        return phenomenonId
    }

    override suspend fun getCognitionInfo(
        cognitionId: Long
    ): CognitionInfo {

        logger.debug {
            "Cognition[$cognitionId]: getting CognitionInfo"
        }

        val cognitionInfo = driver.session().use { session ->

            session.executeRead { tx ->

                val result = tx.runCypher(query = $$"""
                    MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                    OPTIONAL MATCH (parent:Cognition)-[:hasChild]->(cognition)
                    RETURN
                        id(cognition) as id,
                        id(parent) as parentId,
                        cognition.initiationMoment as initiationMoment
                """.trimIndent(),
                    parameters = mapOf(
                        "cognitionId" to cognitionId
                    )
                )

                val record = result.single()

                CognitionInfo(
                    id = record["id"].asLong(),
                    parentId = if (record["parentId"].isNull) null else record["parentId"].asLong(),
                    initiationMoment = record["initiationMoment"].asInstant()
                )
            }

        }

        logger.debug {
            "Cognition[$cognitionId]: retrieved CognitionInfo"
        }

        return cognitionInfo
    }

    override suspend fun getCognitionTitle(
        cognitionId: Long
    ): String? {

        logger.debug {
            "Cognition[$cognitionId]: getting title"
        }

        val title = driver.session().use { session ->

            session.executeRead { tx ->

                val result = tx.runCypher(query = $$"""
                    MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                    RETURN
                        cognition.title as title
                    """.trimIndent(),
                    parameters = mapOf(
                        "cognitionId" to cognitionId
                    )
                )

                val record = result.single()

                record["title"]?.asString()
            }
        }

        logger.debug {
            "Cognition[$cognitionId]: retrieved title: $title"
        }

        return title
    }

    override suspend fun setCognitionTitle(
        cognitionId: Long,
        title: String?
    ) {

        logger.debug {
            "Cognition[$cognitionId]: setting title: $title"
        }

        driver.session().use { session ->

            session.executeWrite { tx ->
                tx.runCypher(
                    query = $$"""
                        MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                        SET cognition.title = $title
                    """.trimIndent(),
                    parameters = mapOf(
                        "cognitionId" to cognitionId,
                        "title" to title
                    )
                ).consume()
            }

        }
    }

    override suspend fun getCognitionSummary(
        cognitionId: Long
    ): String? {

        logger.debug {
            "Cognition[$cognitionId]: getting summary"
        }

        val summary = driver.session().use { session ->

            session.executeRead { tx ->

                val result = tx.runCypher(
                    query = $$"""
                        MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                        RETURN
                            cognition.summary as summary
                    """.trimIndent(), mapOf(
                        "cognitionId" to cognitionId
                    )
                )

                val record = result.single()

                record["summary"]?.asString()
            }
        }

        logger.debug {
            "Cognition[$cognitionId]: retrieved summary: $summary"
        }

        return summary
    }

    override suspend fun setCognitionSummary(
        cognitionId: Long,
        summary: String?
    ) {

        logger.debug {
            "Cognition[$cognitionId]: setting summary: $summary"
        }

        driver.session().use { session ->

            session.executeWrite { tx ->
                tx.runCypher(
                    query = $$"""
                        MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                        SET cognition.summary = $summary
                    """.trimIndent(), mapOf(
                        "cognitionId" to cognitionId,
                        "summary" to summary
                    )
                ).consume()
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
                        MATCH (cognition:Cognition)-[:hasPart]->(expression:PhenomenalExpression)
                        MATCH (agent:EpistemicAgent)-[:creator]->(expression)
                        WHERE id(cognition) = $cognitionId
                        
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
        cognitionId: Long
    ): CulminatedWithIntent? {

        logger.debug {
            "Cognition[$cognitionId]: checking if culminated with an Intent"
        }

        val culminatedWithIntent = driver.session().use { session ->
            session.executeRead { tx ->

                // TODO for sure there is easier way of doing it
                val result = tx.runCypher(
                    query = $$"""
                        MATCH (cognition:Cognition)-[:hasPart]->(expression:PhenomenalExpression)
                        WHERE id(cognition) = $cognitionId
                        
                        WITH max(id(expression)) AS maxExpressionId
                        
                        MATCH (cognition:Cognition)-[:hasPart]->(expression:PhenomenalExpression)-[:hasPart]->(phenomenon:Phenomenon)
                        WHERE id(cognition) = $cognitionId AND id(expression) = maxExpressionId
                        
                        WITH expression, max(id(phenomenon)) AS maxPhenomenonId
                        
                        OPTIONAL MATCH (expression)-[:hasPart]->(maxPhenomenon:Phenomenon:Intent)
                        WHERE id(maxPhenomenon) = maxPhenomenonId
                        
                        RETURN
                            id(expression) AS expressionId,
                            id(maxPhenomenon) AS phenomenonId
                    """.trimIndent(),
                    parameters = mapOf(
                        "cognitionId" to cognitionId
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

        if (culminatedWithIntent == null) {
            logger.debug {
                "Cognition[$cognitionId]: no Intent to fulfill"
            }
        } else {
            logger.debug {
                "Cognition[$cognitionId]/Expression[${culminatedWithIntent.expressionId}]/Phenomenon[${culminatedWithIntent.phenomenonId}]: expression culminated with an intent to fulfill"
            }
        }
        return culminatedWithIntent
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
