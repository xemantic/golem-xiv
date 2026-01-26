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

package com.xemantic.ai.golem.api.backend

import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.script.Cognition
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

data class CognitionInfo(
    val id: Long,
    val parentId: Long?,
    val initiationMoment: Instant
)

data class PhenomenalExpressionInfo(
    val id: Long,
    val initiationMoment: Instant
)

interface CognitionRepository {

    suspend fun initiateCognition(
        constitution: List<String>,
        parentId: Long? = null
    ): CognitionInfo

    suspend fun appendPhenomena(
        cognitionId: Long,
        agent: EpistemicAgent,
        phenomena: List<Phenomenon>
    ): PhenomenalExpression

    suspend fun initiateExpression(
        cognitionId: Long,
        agent: EpistemicAgent
    ): PhenomenalExpressionInfo

    suspend fun initiateTextPhenomenon(
        cognitionId: Long,
        expressionId: Long
    ): Long

    suspend fun initiateIntentPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        systemId: String
    ): Long

    suspend fun initiateFulfillmentPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        intentId: Long,
        systemId: String
    ): Long

    suspend fun updateSystemPhenomena(
        cognitionId: Long,
        phenomena: List<String>
    )

    suspend fun culminateExpression(
        cognitionId: Long,
        expressionId: Long
    ): Instant

    suspend fun getCognition(
        cognitionId: Long
    ): Cognition

    suspend fun maybeCulminatedWithIntent(
        cognitionId: Long
    ): Phenomenon.Intent?

    suspend fun culminateTextPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        text: String
    )

    suspend fun culminateIntentPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        purpose: String,
        code: String
    )

    suspend fun culminateFulfillmentPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        phenomenonId: Long,
        result: String
    )

}

interface CognitiveMemory {

    suspend fun createCognition(
        constitution: List<String>,
        parentId: Long? = null
    ): CognitionInfo

    suspend fun createExpression(
        cognitionId: Long,
        agentId: Long
    ): PhenomenalExpressionInfo

    suspend fun createPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        label: String
    ): Long

    suspend fun createFulfillmentPhenomenon(
        cognitionId: Long,
        expressionId: Long,
        intentId: Long
    ): Long

    suspend fun getCognitionInfo(
        cognitionId: Long
    ): CognitionInfo

    suspend fun getCognitionTitle(
        cognitionId: Long
    ): String?

    suspend fun setCognitionTitle(
        cognitionId: Long,
        title: String?
    )

    suspend fun getCognitionSummary(
        cognitionId: Long
    ): String?

    suspend fun setCognitionSummary(
        cognitionId: Long,
        summary: String?
    )

    fun expressions(
        cognitionId: Long
    ): Flow<PhenomenalExpression>

    suspend fun maybeCulminatedWithIntent(
        cognitionId: Long
    ): CulminatedWithIntent?

    suspend fun appendPhenomenonContent(
        phenomenonId: Long,
        content: String,
        type: StorageType
    )

    suspend fun readPhenomenonContent(
        phenomenonId: Long,
        type: StorageType
    ): String

    suspend fun writePhenomenonContent(
        phenomenonId: Long,
        content: Map<StorageType, String>
    )

}

data class CulminatedWithIntent(
    val expressionId: Long,
    val phenomenonId: Long
)

interface CognitionStorage {

    suspend fun createCognition(
        cognitionId: Long,
        constitution: List<String>
    )

    suspend fun createExpression(
        cognitionId: Long,
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
//        cognitionId: Long,
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
//        cognitionId: Long,
//        expressionId: Long
//    )

}

enum class StorageType {
    TEXT,
    SYSTEM_ID,
    INTENT_PURPOSE,
    INTENT_CODE,
}
