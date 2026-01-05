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

import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface Cognizer2 {

    fun reason(
        constitution: List<String>,
        phenomenalFlow: List<PhenomenalExpression>,
        parameters: Map<String, String>
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

interface GlobalWorkspace {

    fun perceive(
        phenomena: List<Phenomenon>
    ): Int

    fun follow(cognitionId: Int) {

    }

}
