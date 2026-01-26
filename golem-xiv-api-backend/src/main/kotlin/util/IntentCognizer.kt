/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
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

package com.xemantic.ai.golem.api.backend.util

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.json.DefaultStreamingJsonParser
import com.xemantic.ai.golem.json.JsonEvent

/**
 * The intent cognizer is a component to be used in the implementation of [Cognizer] interface.
 *
 * Accumulates purpose and code during streaming and provides the final values
 * when the content block is complete.
 */
class IntentCognizer(
    private val expressionId: Long,
    private val phenomenonId: Long
) {

    private val jsonParser = DefaultStreamingJsonParser()

    private var phase: Phase? = null

    private val purposeBuffer = StringBuilder()
    private val codeBuffer = StringBuilder()

    val purpose: String get() = purposeBuffer.toString()
    val code: String get() = codeBuffer.toString()

    fun add(
        jsonDelta: String
    ): List<CognitionEvent> = jsonParser
        .parse(jsonDelta)
        .flatMap {
            processJsonEvent(it)
        }

    private fun processJsonEvent(
        event: JsonEvent
    ): List<CognitionEvent> = buildList {

        when (event) {

            is JsonEvent.PropertyName -> {
                when (event.name) {
                    "purpose" -> {
                        phase = Phase.COLLECTING_PURPOSE
                        add(CognitionEvent.IntentPurposeInitiation(
                            id = phenomenonId,
                            expressionId = expressionId
                        ))
                    }
                    "code" -> {
                        phase = Phase.COLLECTING_CODE
                        add(CognitionEvent.IntentCodeInitiation(
                            id = phenomenonId,
                            expressionId = expressionId
                        ))
                    }
                    else -> throw IllegalStateException(
                        "Unsupported JSON property: ${event.name}"
                    )
                }
            }

            is JsonEvent.StringDelta -> {
                val chunk = event.chunk
                when (phase) {
                    Phase.COLLECTING_PURPOSE -> {
                        purposeBuffer.append(chunk)
                        add(CognitionEvent.IntentPurposeUnfolding(
                            id = phenomenonId,
                            expressionId = expressionId,
                            purposeDelta = chunk
                        ))
                    }
                    Phase.COLLECTING_CODE -> {
                        codeBuffer.append(chunk)
                        add(CognitionEvent.IntentCodeUnfolding(
                            id = phenomenonId,
                            expressionId = expressionId,
                            codeDelta = chunk
                        ))
                    }
                    null -> throw IllegalStateException( // should never happen
                        "No IntentAccumulator.phase selected, but received JsonEven.StringDelta: $chunk"
                    )
                }
            }

            is JsonEvent.StringEnd -> {
                when (phase) {
                    Phase.COLLECTING_PURPOSE -> {
                        add(CognitionEvent.IntentPurposeCulmination(
                            id = phenomenonId,
                            expressionId = expressionId
                        ))
                    }
                    Phase.COLLECTING_CODE -> {
                        add(CognitionEvent.IntentCodeCulmination(
                            id = phenomenonId,
                            expressionId = expressionId
                        ))
                    }
                    null -> throw IllegalStateException( // should never happen
                        "No IntentAccumulator.phase selected, but received JsonEven.StringEnd"
                    )
                }
            }

            else -> { /* nothing to do */}

        }

    }

    private enum class Phase {
        COLLECTING_PURPOSE,
        COLLECTING_CODE
    }

}
