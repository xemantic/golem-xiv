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
    private val cognitionId: Long,
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
