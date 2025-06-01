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
                    val id = memory.createPhenomenon(expressionId = info.id)
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
    )

    override suspend fun initiateTextPhenomenon(
        workspaceId: Long,
        expressionId: Long
    ): Long = memory.createPhenomenon(workspaceId)

    override suspend fun initiateIntentPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        systemId: String
    ): Long {
        TODO("Not yet implemented")
    }

    override suspend fun initiateFulfilmentPhenomenon(workspaceId: Long, expressionId: Long, systemId: String): Long {
        TODO("Not yet implemented")
    }

    override suspend fun appendText(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        textDelta: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun appendIntentPurpose(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        purposeDelta: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun appendIntentCode(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        codeDelta: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateSystemPhenomena(
        workspaceId: Long,
        phenomena: List<String>
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun culminateExpression(
        workspaceId: Long,
        expressionId: Long
    ): Instant {
        TODO("Not yet implemented")
    }

    override suspend fun getWorkspace(workspaceId: Long): CognitiveWorkspace {
        TODO("Not yet implemented")
    }

}
