/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.cognition

import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.CognitionInfo
import com.xemantic.ai.golem.api.backend.CognitiveMemory
import com.xemantic.ai.golem.api.backend.CognitionRepository
import com.xemantic.ai.golem.api.backend.CognitionStorage
import com.xemantic.ai.golem.api.backend.PhenomenalExpressionInfo
import com.xemantic.ai.golem.api.backend.StorageType
import com.xemantic.ai.golem.api.backend.script.Cognition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant

class DefaultCognitionRepository(
    private val memory: CognitiveMemory,
    private val storage: CognitionStorage
) : CognitionRepository {

    override suspend fun initiateCognition(
        constitution: List<String>,
        parentId: Long?
    ): CognitionInfo = memory.createCognition(
        constitution = constitution,
        parentId = parentId
    )

    override suspend fun appendPhenomena(
        cognitionId: Long,
        agent: EpistemicAgent,
        phenomena: List<Phenomenon>
    ): PhenomenalExpression {

        val info = initiateExpression(cognitionId, agent)

        val persistedPhenomena = phenomena.map { phenomenon ->
            when (phenomenon) {
                is Phenomenon.Text -> {
                    val id = memory.createPhenomenon(
                        cognitionId = cognitionId,
                        expressionId = info.id,
                        label = "Text"
                    )
                    appendText(
                        cognitionId = cognitionId,
                        expressionId = info.id,
                        phenomenonId = id,
                        textDelta = phenomenon.text
                    )
                    Phenomenon.Text(
                        id = id,
                        text = phenomenon.text
                    )
                }
                else -> throw IllegalStateException("Unsupported phenomenon: $phenomenon")
            }
        }

        return PhenomenalExpression(
            id = 0L,
            agent = agent,
            phenomena = persistedPhenomena,
            initiationMoment = info.initiationMoment,
            culminationMoment = info.initiationMoment
        )

    }

    override suspend fun initiateExpression(
        cognitionId: Long,
        agent: EpistemicAgent
    ): PhenomenalExpressionInfo = memory.createExpression(
        cognitionId = cognitionId,
        agentId = agent.id
    ).also { info ->
        storage.createExpression(
            cognitionId,
            info.id
        )
    }

    override suspend fun initiateTextPhenomenon(
        cognitionId: Long,
        expressionId: Long
    ): Long = memory.createPhenomenon(
        cognitionId = cognitionId,
        expressionId = expressionId,
        label = "Text"
    )

    override suspend fun initiateIntentPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        systemId: String
    ): Long {
        val phenomenonId = memory.createPhenomenon(
            cognitionId = cognitionId,
            expressionId = expressionId,
            label = "Intent"
        )
        storage.append(
            cognitionId = cognitionId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = systemId,
            type = StorageType.SYSTEM_ID
        )
        return phenomenonId
    }

    override suspend fun initiateFulfillmentPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        intentId: Long,
        systemId: String
    ): Long {
        val phenomenonId = memory.createFulfillmentPhenomenon(
            cognitionId = cognitionId,
            expressionId = expressionId,
            intentId = intentId
        )
        storage.append(
            cognitionId = cognitionId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = systemId,
            type = StorageType.SYSTEM_ID
        )
        return phenomenonId
    }

    override suspend fun appendText(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        textDelta: String
    ) {
        storage.append(
            cognitionId = cognitionId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = textDelta,
            type = StorageType.TEXT
        )
    }

    override suspend fun appendIntentPurpose(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        purposeDelta: String
    ) {
        storage.append(
            cognitionId = cognitionId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = purposeDelta,
            type = StorageType.INTENT_PURPOSE
        )
    }

    override suspend fun appendIntentCode(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        codeDelta: String
    ) {
        storage.append(
            cognitionId = cognitionId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = codeDelta,
            type = StorageType.INTENT_CODE
        )
    }

    override suspend fun updateSystemPhenomena(
        cognitionId: Long,
        phenomena: List<String>
    ) {
        // TOOD it seems that only culmination date needs to be added
        TODO("Not yet implemented")
    }

    override suspend fun culminateExpression(
        cognitionId: Long,
        expressionId: Long
    ): Instant {
        // this should just add date, not implemented at the moment
        // TODO implement it in neo4j
        return Clock.System.now()
    }

    override suspend fun getCognition(
        cognitionId: Long
    ): Cognition {

        val info = memory.getCognitionInfo(cognitionId)

        return object : Cognition {

            override val id: Long = info.id

            override val initiationMoment: Instant = info.initiationMoment

            override val parentId: Long? = info.parentId

            override suspend fun getTitle(): String? = memory.getCognitionTitle(
                cognitionId
            )

            override suspend fun setTitle(
                title: String?
            ) = memory.setCognitionTitle(
                cognitionId, title
            )

            override suspend fun getSummary(): String? = memory.getCognitionSummary(
                cognitionId
            )

            override suspend fun setSummary(
                summary: String?
            ) = memory.setCognitionSummary(
                cognitionId, summary
            )

            override fun expressions(): Flow<PhenomenalExpression> = memory.expressions(
                cognitionId
            ).map { expression ->
                expression.copy(
                    phenomena = expression.phenomena.map { phenomenon ->
                        when (phenomenon) {
                            is Phenomenon.Text -> Phenomenon.Text(
                                id = phenomenon.id,
                                text = storage.readPhenomenonComponent(
                                    cognitionId = cognitionId,
                                    expressionId = expression.id,
                                    phenomenonId = phenomenon.id,
                                    type = StorageType.TEXT
                                )
                            )
                            is Phenomenon.Intent -> Phenomenon.Intent(
                                id = phenomenon.id,
                                systemId = storage.readPhenomenonComponent(
                                    cognitionId = cognitionId,
                                    expressionId = expression.id,
                                    phenomenonId = phenomenon.id,
                                    type = StorageType.SYSTEM_ID
                                ),
                                purpose = storage.readPhenomenonComponent(
                                    cognitionId = cognitionId,
                                    expressionId = expression.id,
                                    phenomenonId = phenomenon.id,
                                    type = StorageType.INTENT_PURPOSE
                                ),
                                code = storage.readPhenomenonComponent(
                                    cognitionId = cognitionId,
                                    expressionId = expression.id,
                                    phenomenonId = phenomenon.id,
                                    type = StorageType.INTENT_CODE
                                )
                            )
                            is Phenomenon.Fulfillment -> Phenomenon.Fulfillment(
                                id = phenomenon.id,
                                intentId = phenomenon.intentId,
                                intentSystemId = storage.readPhenomenonComponent(
                                    cognitionId = cognitionId,
                                    expressionId = expression.id,
                                    phenomenonId = phenomenon.id,
                                    type = StorageType.SYSTEM_ID
                                ),
                                result = storage.readPhenomenonComponent(
                                    cognitionId = cognitionId,
                                    expressionId = expression.id,
                                    phenomenonId = phenomenon.id,
                                    type = StorageType.TEXT
                                )
                            )
                            else -> throw IllegalStateException("Unsupported phenomenon: $phenomenon")
                        }
                    }
                )
            }

        }

    }

    override suspend fun maybeCulminatedWithIntent(
        cognitionId: Long
    ): Phenomenon.Intent? {
        val culminatedWithIntent = memory.maybeCulminatedWithIntent(
            cognitionId = cognitionId
        )
        return if (culminatedWithIntent != null) {

            suspend fun read(type: StorageType) = storage.readPhenomenonComponent(
                cognitionId = cognitionId,
                expressionId = culminatedWithIntent.expressionId,
                phenomenonId = culminatedWithIntent.phenomenonId,
                type = type
            )

            val systemId = read(type = StorageType.SYSTEM_ID)
            val purpose = read(type = StorageType.INTENT_PURPOSE)
            val code = read(type = StorageType.INTENT_CODE)

            Phenomenon.Intent(
                id = culminatedWithIntent.phenomenonId,
                systemId = systemId,
                purpose = purpose,
                code = code
            )
        } else {
            null
        }
    }

}
