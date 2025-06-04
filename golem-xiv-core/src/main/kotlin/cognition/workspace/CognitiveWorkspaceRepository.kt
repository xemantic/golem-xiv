/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.cognition.workspace

import com.xemantic.ai.golem.api.CognitiveWorkspace
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceInfo
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceMemory
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceRepository
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceStorage
import com.xemantic.ai.golem.api.backend.PhenomenalExpressionInfo
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

class DefaultCognitiveWorkspaceRepository(
    private val memory: CognitiveWorkspaceMemory,
    private val storage: CognitiveWorkspaceStorage
) : CognitiveWorkspaceRepository {

    override suspend fun initiateWorkspace(
        conditioning: List<String>,
        parentId: Long?
    ): CognitiveWorkspaceInfo {
        val workspaceInfo = memory.createWorkspace(parentId)
        storage.createWorkspace(
            workspaceId = workspaceInfo.id,
            conditioning = conditioning
        )
        return workspaceInfo
    }

    override suspend fun appendToWorkspace(
        workspaceId: Long,
        agent: EpistemicAgent,
        phenomena: List<Phenomenon>
    ): PhenomenalExpression {

        val info = initiateExpression(workspaceId, agent)

        val persistedPhenomena = phenomena.map { phenomenon ->
            when (phenomenon) {
                is Phenomenon.Text -> {
                    val id = memory.createPhenomenon(
                        workspaceId = workspaceId,
                        expressionId = info.id,
                        label = "Text"
                    )
                    appendText(
                        workspaceId = workspaceId,
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
        workspaceId: Long,
        agent: EpistemicAgent
    ): PhenomenalExpressionInfo = memory.createExpression(
        workspaceId = workspaceId,
        agentId = agent.id
    ).also { info ->
        storage.createExpression(
            workspaceId,
            info.id
        )
    }

    override suspend fun initiateTextPhenomenon(
        workspaceId: Long,
        expressionId: Long
    ): Long = memory.createPhenomenon(
        workspaceId = workspaceId,
        expressionId = expressionId,
        label = "Text"
    )

    override suspend fun initiateIntentPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        systemId: String
    ): Long {
        val phenomenonId = memory.createPhenomenon(
            workspaceId = workspaceId,
            expressionId = expressionId,
            label = "Intent"
        )
        storage.append(
            workspaceId = workspaceId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = systemId,
            classifier = "system"
        )
        return phenomenonId
    }

    override suspend fun initiateFulfilmentPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        systemId: String
    ): Long {
        val phenomenonId = memory.createPhenomenon(
            workspaceId = workspaceId,
            expressionId = expressionId,
            label = "Fulfilment"
        )
        storage.append(
            workspaceId = workspaceId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = systemId,
            classifier = "system"
        )
        return phenomenonId
    }

    override suspend fun appendText(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        textDelta: String
    ) {
        storage.append(
            workspaceId = workspaceId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = textDelta
        )
    }

    override suspend fun appendIntentPurpose(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        purposeDelta: String
    ) {
        storage.append(
            workspaceId = workspaceId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = purposeDelta,
            classifier = "intent-purpose"
        )
    }

    override suspend fun appendIntentCode(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        codeDelta: String
    ) {
        storage.append(
            workspaceId = workspaceId,
            expressionId = expressionId,
            phenomenonId = phenomenonId,
            textDelta = codeDelta,
            classifier = "intent-code"
        )
    }

    override suspend fun updateSystemPhenomena(
        workspaceId: Long,
        phenomena: List<String>
    ) {
        // TOOD it seems that only culmination date needs to be added
        TODO("Not yet implemented")
    }

    override suspend fun culminateExpression(
        workspaceId: Long,
        expressionId: Long
    ): Instant {
        TODO("Not yet implemented")
    }

    override suspend fun getWorkspace(
        workspaceId: Long
    ): CognitiveWorkspace {
        val info = memory.getWorkspaceInfo(workspaceId)

        return object : CognitiveWorkspace {

            override val id: Long = info.id

            override val initiationMoment: Instant = info.initiationMoment

            override suspend fun getTitle(): String? = memory.getWorkspaceTitle(
                workspaceId
            )

            override suspend fun setTitle(
                title: String?
            ) = memory.setWorkspaceTitle(
                workspaceId, title
            )

            override suspend fun getSummary(): String? = memory.getWorkspaceSummary(
                workspaceId
            )

            override suspend fun setSummary(
                summary: String?
            ) = memory.setWorkspaceSummary(
                workspaceId, summary
            )

            override fun expressions(): Flow<PhenomenalExpression> = memory.expressions(
                workspaceId
            )

        }

    }

    override suspend fun maybeCulminatedWithIntent(
        workspaceId: Long
    ): Phenomenon.Intent? {
        val culminatedWithIntent = memory.maybeCulminatedWithIntent(
            workspaceId = workspaceId
        )
        return if (culminatedWithIntent != null) {

            suspend fun read(classifier: String) = storage.readPhenomenon(
                workspaceId = workspaceId,
                expressionId = culminatedWithIntent.expressionId,
                phenomenonId = culminatedWithIntent.phenomenonId,
                classifier = classifier
            )

            val systemId = read("systemId")
            val purpose = read("purpose")
            val code = read("code")

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
