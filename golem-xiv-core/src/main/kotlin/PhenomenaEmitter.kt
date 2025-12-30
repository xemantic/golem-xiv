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

package com.xemantic.ai.golem.core

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import kotlinx.coroutines.flow.FlowCollector
import kotlin.time.Clock

internal suspend fun FlowCollector<CognitionEvent>.emit(
    expression: PhenomenalExpression
) {

    emit(CognitionEvent.ExpressionInitiation(
        expressionId = expression.id,
        agent = expression.agent,
        moment = expression.initiationMoment
    ))

    expression.phenomena.forEach { phenomenon ->
        when (phenomenon) {
            is Phenomenon.Text -> {
                emit(CognitionEvent.TextInitiation(
                    id = phenomenon.id,
                    expressionId = expression.id
                ))
                // TODO does it make sense to chunk it?
                emit(CognitionEvent.TextUnfolding(
                    id = phenomenon.id,
                    expressionId = expression.id,
                    textDelta = phenomenon.text
                ))
                emit(CognitionEvent.TextCulmination(
                    id = phenomenon.id,
                    expressionId = expression.id
                ))
            }
            is Phenomenon.Intent -> {
                emit(CognitionEvent.IntentInitiation(
                    id = phenomenon.id,
                    expressionId = expression.id,
                    systemId = phenomenon.systemId
                ))
                emit(CognitionEvent.IntentPurposeInitiation(
                    id = phenomenon.id,
                    expressionId = expression.id
                ))
                emit(CognitionEvent.IntentPurposeUnfolding(
                    id = phenomenon.id,
                    expressionId = expression.id,
                    purposeDelta = phenomenon.purpose
                ))
                emit(CognitionEvent.IntentPurposeCulmination(
                    id = phenomenon.id,
                    expressionId = expression.id,
                ))
                emit(CognitionEvent.IntentCodeInitiation(
                    id = phenomenon.id,
                    expressionId = expression.id
                ))
                emit(CognitionEvent.IntentCodeUnfolding(
                    id = phenomenon.id,
                    expressionId = expression.id,
                    codeDelta = phenomenon.code
                ))
                emit(CognitionEvent.IntentCodeCulmination(
                    id = phenomenon.id,
                    expressionId = expression.id,
                ))
                emit(CognitionEvent.IntentCulmination(
                    id = phenomenon.id,
                    expressionId = expression.id,
                ))
            }
            is Phenomenon.Fulfillment -> {
                emit(CognitionEvent.FulfillmentInitiation( // TODO system id is missing
                    id = phenomenon.id,
                    expressionId = expression.id,
                    intentId = phenomenon.intentId,
                    intentSystemId = phenomenon.intentSystemId
                ))
                emit(CognitionEvent.FulfillmentUnfolding(
                    id = phenomenon.id,
                    expressionId = expression.id,
                    textDelta = phenomenon.result
                ))
                emit(CognitionEvent.FulfillmentCulmination(
                    id = phenomenon.id,
                    expressionId = expression.id,
                    impeded = phenomenon.impeded
                ))
            }
            else -> throw IllegalStateException("Unsupported phenomenon: $phenomenon")
        }
    }

    emit(
        CognitionEvent.ExpressionCulmination(
            expressionId = expression.id,
            //moment = expression.culminationMoment!!,
            moment = Clock.System.now() // TODO we should store culmination moment properly
        )
    )

}

internal fun FlowCollector<GolemOutput>.cognitionBroadcaster(
    cognitionId: Long
) = FlowCollector<CognitionEvent> { value ->
    emit(GolemOutput.Cognition(
        cognitionId = cognitionId,
        event = value
    ))
}
