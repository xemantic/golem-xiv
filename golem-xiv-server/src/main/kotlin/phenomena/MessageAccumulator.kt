/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.phenomena

import com.xemantic.ai.golem.api.Agent
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.CognitionEvent.*
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.Expression
import kotlin.time.Clock
import kotlin.time.Instant

class ExpressionAccumulator(
    private val workspaceId: String
) {

    private val textBuilder = StringBuilder()

    private val phenomena = mutableListOf<Phenomenon>()

    private var id: String? = null

    private var initiationMoment: Instant? = null

    private var intentSystemId: String? = null

    private var intentPurpose: String? = null

    operator fun plusAssign(event: CognitionEvent) {
        event.process()
    }

    fun build() = Expression(
        id = id!!,
        agent = Agent( // TODO we need a better way of assigning it in the future.
            id = "golem",
            description = "The agent",
            category = Agent.Category.SELF
        ),
        phenomena = phenomena,
        initiationMoment = initiationMoment!!,
        culminationMoment = Clock.System.now()
    )

    private fun CognitionEvent.process() = when (this) {
        is ExpressionInitiation -> {
            id = expressionId
            initiationMoment = Clock.System.now()
        }
        is TextInitiation -> { /* nothing to do */ }
        is TextUnfolding -> textBuilder.append(textDelta)
        is TextCulmination -> culminate { text -> Phenomenon.Text(id!!, text) }
        is IntentInitiation -> {
            intentSystemId = systemId
            intentPurpose = purpose
        }
        is IntentUnfolding -> textBuilder.append(instructionsDelta)
        is IntentCulmination -> culminate { text ->
            Phenomenon.Intent(
                id = id!!,
                systemId = intentSystemId!!,
                purpose = intentPurpose!!,
                instructions = text
            )
        }
        is ExpressionCulmination -> { /* nothing to do */ }
        else -> { /* other cognitive events are not used this context */ }
    }

    private fun culminate(block: (text: String) -> Phenomenon) {
        val text = textBuilder.toString()
        val phenomenon = block(text)
        phenomena += phenomenon
        textBuilder.clear()
        intentSystemId = null
        intentPurpose = null
    }

}
