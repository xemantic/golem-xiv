/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import kotlinx.coroutines.flow.FlowCollector

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
            else -> throw IllegalStateException("Unsupported phenomenon: $phenomenon")
        }
    }

    emit(
        CognitionEvent.ExpressionCulmination(
            expressionId = expression.id,
            moment = expression.culminationMoment!!,
        )
    )

}

internal fun FlowCollector<GolemOutput>.cognitionBroadcaster(
    workspaceId: Long
) = FlowCollector<CognitionEvent> { value ->
    emit(GolemOutput.Cognition(
        workspaceId = workspaceId,
        event = value
    ))
}
