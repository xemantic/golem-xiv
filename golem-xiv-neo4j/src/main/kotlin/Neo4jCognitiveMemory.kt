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

import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.CognitionInfo
import com.xemantic.ai.golem.api.backend.CognitiveMemory
import com.xemantic.ai.golem.api.backend.CulminatedWithIntent
import com.xemantic.ai.golem.api.backend.PhenomenalExpressionInfo
import com.xemantic.ai.golem.api.backend.StorageType
import com.xemantic.neo4j.driver.Neo4jOperations
import com.xemantic.neo4j.driver.asInstant
import com.xemantic.neo4j.driver.singleOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.neo4j.driver.types.Node

class Neo4jCognitiveMemory(
    private val neo4j: Neo4jOperations
) : CognitiveMemory {

    private val logger = KotlinLogging.logger {}

    override suspend fun createCognition(
        constitution: List<String>,
        parentId: Long?,
    ): CognitionInfo = neo4j.write { tx ->
        if (parentId != null) {
            tx.run(
                query = $$"""
                    MATCH (parent:Cognition) WHERE id(parent) = $parentId
                    CREATE (cognition:Cognition {
                        title: 'Untitled',
                        summary: '',
                        constitution: $constitution,
                        initiationMoment: datetime()
                    })
                    CREATE (parent)-[:hasChild]->(cognition)
                    RETURN
                        id(cognition) AS id,
                        cognition.initiationMoment AS initiationMoment
                """.trimIndent(),
                parameters = mapOf(
                    "parentId" to parentId,
                    "constitution" to constitution
                )
            ).single()
        } else {
            tx.run(
                query = $$"""
                CREATE (cognition:Cognition {
                    constitution: $constitution,
                    initiationMoment: datetime()
                })
                RETURN
                    id(cognition) AS id,
                    cognition.initiationMoment AS initiationMoment
            """.trimIndent(),
                parameters = mapOf(
                    "constitution" to constitution
                )
            ).single()
        }.let {
            CognitionInfo(
                id = it["id"].asLong(),
                parentId = parentId,
                initiationMoment = it["initiationMoment"].asInstant(),
            )
        }
    }

    override suspend fun createExpression(
        cognitionId: Long,
        agentId: Long,
    ): PhenomenalExpressionInfo = neo4j.write { tx ->
        tx.run(
            query = $$"""
                MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                MATCH (agent:EpistemicAgent) WHERE id(agent) = $agentId
                CREATE (expression:PhenomenalExpression {
                    title: 'Expression',
                    initiationMoment: datetime()
                })
                SET expression.title = expression.title + ' ' + id(expression)
                CREATE (agent)-[:creator]->(expression)
                CREATE (cognition)-[:hasPart]->(expression)
                RETURN
                    id(expression) AS id,
                    expression.initiationMoment AS initiationMoment
            """.trimIndent(),
            parameters = mapOf(
                "cognitionId" to cognitionId,
                "agentId" to agentId
            )
        ).single().let {
            PhenomenalExpressionInfo(
                id = it["id"].asLong(),
                initiationMoment = it["initiationMoment"].asInstant()
            )
        }
    }

    override suspend fun createPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        label: String
    ): Long {

        logger.debug {
            "Cognition[$cognitionId]/Expression[$expressionId]: creating Phenomenon ($label)"
        }

        val phenomenonId = neo4j.write { tx ->
            tx.run(
                query = $$"""
                    MATCH (expression:PhenomenalExpression) WHERE id(expression) = $expressionId
                    CREATE (phenomenon:Phenomenon:$$label)
                    SET phenomenon.title = '$$label ' + id(phenomenon)
                    CREATE (expression)-[:hasPart]->(phenomenon)
                    RETURN
                        id(phenomenon) AS id
                """.trimIndent(),
                parameters = mapOf(
                    "expressionId" to expressionId
                )
            ).single()["id"].asLong()
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

        val phenomenonId = neo4j.write { tx ->
            tx.run(
                query = $$"""
                    MATCH (expression:PhenomenalExpression) WHERE id(expression) = $expressionId
                    CREATE (fulfillment:Phenomenon:Fulfillment)
                    SET fulfillment.title = 'Fulfillment ' + id(fulfillment)
                    CREATE (expression)-[:hasPart]->(fulfillment)
                    
                    WITH fulfillment
                    MATCH (intent:Phenomenon:Intent) WHERE id(intent) = $intentId
                    CREATE (fulfillment)-[:fulfills]->(intent)
                    
                    RETURN
                        id(fulfillment) AS id
                """.trimIndent(),
                parameters = mapOf(
                    "expressionId" to expressionId,
                    "intentId" to intentId
                )
            ).single()["id"].asLong()
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

        val cognitionInfo = neo4j.read { tx ->
            val record = tx.run(
                query = $$"""
                MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                OPTIONAL MATCH (parent:Cognition)-[:hasChild]->(cognition)
                RETURN
                    id(cognition) AS id,
                    id(parent) AS parentId,
                    cognition.initiationMoment AS initiationMoment
            """.trimIndent(),
                parameters = mapOf(
                    "cognitionId" to cognitionId
                )
            ).single()

            CognitionInfo(
                id = record["id"].asLong(),
                parentId = if (record["parentId"].isNull) null else record["parentId"].asLong(),
                initiationMoment = record["initiationMoment"].asInstant()
            )
        }

        logger.debug {
            "Cognition[$cognitionId]: retrieved CognitionInfo"
        }

        return cognitionInfo
    }

    override suspend fun getCognitionTitle(
        cognitionId: Long
    ): String? = neo4j.read { tx ->
        tx.run(
            query = $$"""
                MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                RETURN cognition.title AS title
            """.trimIndent(),
            parameters = mapOf(
                "cognitionId" to cognitionId
            )
        ).singleOrNull()?.get("title")?.asString()
    }

    override suspend fun setCognitionTitle(
        cognitionId: Long,
        title: String?
    ) {
        neo4j.write { tx ->
            tx.run(
                query = $$"""
                    MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                    SET cognition.title = $title
                """.trimIndent(),
                parameters = mapOf(
                    "cognitionId" to cognitionId,
                    "title" to title
                )
            )
        }
    }

    override suspend fun getCognitionSummary(
        cognitionId: Long
    ): String? = neo4j.read { tx ->
        tx.run(
            query = $$"""
                MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                RETURN
                    cognition.summary AS summary
            """.trimIndent(), mapOf(
                "cognitionId" to cognitionId
            )
        ).singleOrNull()?.get("summary")?.asString()
    }

    override suspend fun setCognitionSummary(
        cognitionId: Long,
        summary: String?
    ) {
        neo4j.write { tx ->
            tx.run(
                query = $$"""
                    MATCH (cognition:Cognition) WHERE id(cognition) = $cognitionId
                    SET cognition.summary = $summary
                """.trimIndent(), mapOf(
                    "cognitionId" to cognitionId,
                    "summary" to summary
                )
            )
        }
    }

    override fun expressions(
        cognitionId: Long
    ): Flow<PhenomenalExpression> = neo4j.flow(
        query = $$"""
            MATCH (cognition:Cognition)-[:hasPart]->(expression:PhenomenalExpression)
            MATCH (agent:EpistemicAgent)-[:creator]->(expression)
            WHERE id(cognition) = $cognitionId
            
            OPTIONAL MATCH (expression)-[:hasPart]->(phenomenon:Phenomenon)
            OPTIONAL MATCH (phenomenon)-[:fulfills]->(intent:Phenomenon:Intent)
            
            WITH expression, agent, phenomenon, intent
            ORDER BY id(expression), id(phenomenon)
            
            RETURN
                id(expression) AS expressionId,
                expression.initiationMoment AS initiationMoment,
                id(agent) AS agentId,
                agent AS agent,
                labels(agent) AS agentLabels,
                collect({
                    phenomenon: phenomenon,
                    intent: intent
                }) AS phenomenaWithIntents
            ORDER BY id(expression)
        """.trimIndent(),
        parameters = mapOf(
            "cognitionId" to cognitionId
        )
    ).map { record ->

        val epistemicAgent = toEpistemicAgent(
            id = record["agentId"].asLong(),
            agent = record["agent"].asNode(),
            agentLabels = record["agentLabels"].asList {
                it.asString()
            }.toSet()
        )

        val phenomena = record["phenomenaWithIntents"].asList { item ->
            val itemMap = item.asMap()
            val phenomenonValue = itemMap["phenomenon"]
            val node = phenomenonValue as? Node ?: return@asList null
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

                else -> throw java.lang.IllegalStateException(
                    "Unsupported phenomenon: $labels"
                )
            }
        }.filterNotNull()

        PhenomenalExpression(
            id = record["expressionId"].asLong(),
            agent = epistemicAgent,
            phenomena = phenomena,
            initiationMoment = record["initiationMoment"].asInstant()
        )

    }

    override suspend fun maybeCulminatedWithIntent(
        cognitionId: Long
    ): CulminatedWithIntent? {

        val culminatedWithIntent = neo4j.read { tx ->
            tx.run(
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
            ).singleOrNull()?.let { record ->
                val idValue = record["phenomenonId"]
                if (idValue.isNull) {
                    null
                } else {
                    CulminatedWithIntent(
                        expressionId = record["expressionId"].asLong(),
                        phenomenonId = idValue.asLong()
                    )
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

    override suspend fun appendPhenomenonContent(
        phenomenonId: Long,
        content: String,
        type: StorageType
    ) {
        val propertyName = type.toPropertyName()
        neo4j.write { tx ->
            tx.run(
                query = $$"""
                    MATCH (phenomenon:Phenomenon) WHERE id(phenomenon) = $phenomenonId
                    SET phenomenon.$$propertyName = coalesce(phenomenon.$$propertyName, '') + $content
                """.trimIndent(),
                parameters = mapOf(
                    "phenomenonId" to phenomenonId,
                    "content" to content
                )
            )
        }
    }

    override suspend fun readPhenomenonContent(
        phenomenonId: Long,
        type: StorageType
    ): String {
        val propertyName = type.toPropertyName()
        return neo4j.read { tx ->
            tx.run(
                query = $$"""
                    MATCH (phenomenon:Phenomenon) WHERE id(phenomenon) = $phenomenonId
                    RETURN phenomenon.$$propertyName AS content
                """.trimIndent(),
                parameters = mapOf(
                    "phenomenonId" to phenomenonId
                )
            ).single()["content"].let { value ->
                if (value.isNull) "" else value.asString()
            }
        }
    }

}

private fun StorageType.toPropertyName(): String = when (this) {
    StorageType.TEXT -> "text"
    StorageType.SYSTEM_ID -> "systemId"
    StorageType.INTENT_PURPOSE -> "purpose"
    StorageType.INTENT_CODE -> "code"
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
