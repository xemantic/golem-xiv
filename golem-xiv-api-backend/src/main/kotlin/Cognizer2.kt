/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.backend

import com.xemantic.ai.golem.api.PhenomenalExpression
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface Cognizer2 {

    fun reason(
        constitution: List<String>,
        cognitionId: Long,
        phenomenalFlow: List<PhenomenalExpression>,
        hints: Map<String, String>
    ): Flow<CognizerEvent>

}

sealed interface CognizerEvent {

    object TextStart : CognizerEvent

    data class TextDelta(val text: String) : CognizerEvent

    object TextEnd : CognizerEvent

    data class Usage(
        val categories: Map<String, UsageWithCost>,
    ) : CognizerEvent

}

data class UsageWithCost(
    val usage: Int,
    val cost: BigDecimal
)
