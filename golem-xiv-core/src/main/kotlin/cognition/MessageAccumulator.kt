/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.cognition

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.CognitionEvent.*
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.PhenomenalExpression
import kotlin.time.Clock
import kotlin.time.Instant

// TODO should it be part of the backend API?
class ExpressionAccumulator(
    private val workspaceId: Long,
    private val expressionId: Long
) {

    private val textBuilder = StringBuilder()

    private val phenomena = mutableListOf<Phenomenon>()

    private var initiationMoment: Instant? = null

    private var intentSystemId: String? = null

    private var intentPurpose: String? = null

    private var intentCode: String? = null

    operator fun plusAssign(event: CognitionEvent) {
        event.process()
    }

    fun build() = PhenomenalExpression(
        id = expressionId,
        agent = EpistemicAgent.AI( // TODO we need a better way of assigning it in the future.
            id = -1,
            model = "",
            vendor = ""
        ),
        phenomena = phenomena,
        initiationMoment = initiationMoment!!,
        culminationMoment = Clock.System.now()
    )

    private fun CognitionEvent.process() = when (this) {
        is ExpressionInitiation -> {
            initiationMoment = Clock.System.now()
        }
        is TextInitiation -> { /* nothing to do */ }
        is TextUnfolding -> textBuilder.append(textDelta)
        is TextCulmination -> culminate { text -> Phenomenon.Text(id!!, text) }
        is IntentInitiation -> {
            intentSystemId = systemId
        }
        is IntentPurposeInitiation -> { /* nothing to do */ }
        is IntentPurposeUnfolding -> textBuilder.append(purposeDelta)
        is IntentPurposeCulmination -> {
            intentPurpose = textBuilder.toString()
            textBuilder.clear()
        }
        is IntentCodeInitiation -> { /* nothing to do */ }
        is IntentCodeUnfolding -> textBuilder.append(codeDelta)
        is IntentCodeCulmination -> {
            intentCode = textBuilder.toString()
            textBuilder.clear()
        }
        is IntentCulmination -> culminate { text -> // TODO for some reason it is triggered twice
            Phenomenon.Intent(
                id = id!!,
                systemId = intentSystemId!!,
                purpose = intentPurpose!!,
                code = intentCode!!
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
