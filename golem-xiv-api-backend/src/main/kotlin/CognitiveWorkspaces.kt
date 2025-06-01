/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.backend

import com.xemantic.ai.golem.api.CognitiveWorkspace
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import kotlin.time.Instant

data class CognitiveWorkspaceInfo(
    val id: Long,
    val initiationMoment: Instant
)

data class PhenomenalExpressionInfo(
    val id: Long,
    val initiationMoment: Instant
)

interface CognitiveWorkspaceRepository {

    suspend fun initiateWorkspace(
        conditioning: List<String>,
        parentId: Long? = null
    ): CognitiveWorkspaceInfo

    suspend fun appendToWorkspace(
        workspaceId: Long,
        agent: EpistemicAgent,
        phenomena: List<Phenomenon>
    ): PhenomenalExpression

    suspend fun initiateExpression(
        workspaceId: Long,
        agent: EpistemicAgent
    ): PhenomenalExpressionInfo

    suspend fun initiateTextPhenomenon(
        workspaceId: Long,
        expressionId: Long
    ): Long

    suspend fun initiateIntentPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        systemId: String
    ): Long

    suspend fun initiateFulfilmentPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        systemId: String
    ): Long

    suspend fun appendText(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        textDelta: String
    )

    suspend fun appendIntentPurpose(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        purposeDelta: String
    )

    suspend fun appendIntentCode(
        workspaceId: Long,
        expressionId: Long,
        phenomenonId: Long,
        codeDelta: String
    )

    suspend fun updateSystemPhenomena(
        workspaceId: Long,
        phenomena: List<String>
    )

    suspend fun culminateExpression(
        workspaceId: Long,
        expressionId: Long
    ): Instant

    suspend fun getWorkspace(
        workspaceId: Long
    ): CognitiveWorkspace

}

interface CognitiveWorkspaceMemory {

    suspend fun createWorkspace(
        parentId: Long? = null
    ): CognitiveWorkspaceInfo

    suspend fun createExpression(
        workspaceId: Long,
        agentId: Long
    ): PhenomenalExpressionInfo

    suspend fun createPhenomenon(
        expressionId: Long
    ): Long

    suspend fun updateWorkspace(
        workspaceId: Long,
        title: String?,
        summary: String?
    )

}

interface CognitiveWorkspaceStorage {

    suspend fun createWorkspace(
        workspaceId: Long,
        conditioning: List<String>
    )

    suspend fun addExpression(
        workspaceId: Long,
        expressionId: Long,
        phenomena: List<Phenomenon>
    )

    suspend fun append(
        phenomena: List<Phenomenon>
    )

    // TODO append delta

    suspend fun commit(
        workspaceId: Long,
        expressionId: Long
    )

}

enum class PhenomenonType {
    TEXT,
    INTENT,
}
