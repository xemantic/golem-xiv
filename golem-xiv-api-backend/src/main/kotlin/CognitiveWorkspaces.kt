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
import kotlinx.coroutines.flow.Flow
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

    suspend fun initiateFulfillmentPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        intentId: Long,
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

    suspend fun maybeCulminatedWithIntent(
        workspaceId: Long
    ): Phenomenon.Intent?

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
        workspaceId: Long,
        expressionId: Long,
        label: String
    ): Long

    suspend fun createFulfillmentPhenomenon(
        workspaceId: Long,
        expressionId: Long,
        intentId: Long
    ): Long

    suspend fun getWorkspaceInfo(
        workspaceId: Long
    ): CognitiveWorkspaceInfo

    suspend fun getWorkspaceTitle(
        workspaceId: Long
    ): String?

    suspend fun setWorkspaceTitle(
        workspaceId: Long,
        title: String?
    )

    suspend fun getWorkspaceSummary(
        workspaceId: Long
    ): String?

    suspend fun setWorkspaceSummary(
        workspaceId: Long,
        summary: String?
    )

    fun expressions(
        cognitionId: Long
    ): Flow<PhenomenalExpression>

    suspend fun maybeCulminatedWithIntent(
        workspaceId: Long
    ): CulminatedWithIntent?

}

data class CulminatedWithIntent(
    val expressionId: Long,
    val phenomenonId: Long
)

interface CognitiveWorkspaceStorage {

    suspend fun createWorkspace(
        workspaceId: Long,
        conditioning: List<String>
    )

    suspend fun createExpression(
        workspaceId: Long,
        expressionId: Long
    )

    suspend fun append(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        textDelta: String,
        type: StorageType
    )

    suspend fun readPhenomenonComponent(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        type: StorageType
    ): String

    //    // TODO it is not used at the moment
//    suspend fun addExpression(
//        workspaceId: Long,
//        expressionId: Long,
//        phenomena: List<Phenomenon>
//    )

//    // TODO it is not used at the moment
//    suspend fun append(
//        phenomena: List<Phenomenon>
//    )

//    // TODO append delta
//
//    suspend fun commit(
//        workspaceId: Long,
//        expressionId: Long
//    )

}

enum class StorageType {
    TEXT,
    SYSTEM_ID,
    INTENT_PURPOSE,
    INTENT_CODE,
}
