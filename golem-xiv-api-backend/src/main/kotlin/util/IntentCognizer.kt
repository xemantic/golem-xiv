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

package com.xemantic.ai.golem.api.backend.util

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.backend.CognitionRepository
import com.xemantic.ai.golem.json.DefaultStreamingJsonParser
import com.xemantic.ai.golem.json.JsonEvent

/**
 * The intent cognizer is a component to be used in the implementation of [Cognizer] interface.
 */
class IntentCognizer(
    private val cognitionId: Long,
    private val expressionId: Long,
    private val phenomenonId: Long,
    private val repository: CognitionRepository
) {

    private val jsonParser = DefaultStreamingJsonParser()

    private var phase: Phase? = null

    suspend fun add(
        jsonDelta: String
    ): List<CognitionEvent> = jsonParser
        .parse(jsonDelta)
        .flatMap {
            processJsonEvent(it)
        }

    private suspend fun processJsonEvent(
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
                        repository.appendIntentPurpose(
                            cognitionId = cognitionId,
                            expressionId = expressionId,
                            phenomenonId = phenomenonId,
                            purposeDelta = event.chunk
                        )
                        add(CognitionEvent.IntentPurposeUnfolding(
                            id = phenomenonId,
                            expressionId = expressionId,
                            purposeDelta = event.chunk
                        ))
                    }
                    Phase.COLLECTING_CODE -> {
                        repository.appendIntentCode(
                            cognitionId = cognitionId,
                            expressionId = expressionId,
                            phenomenonId = phenomenonId,
                            codeDelta = event.chunk
                        )
                        add(CognitionEvent.IntentCodeUnfolding(
                            id = phenomenonId,
                            expressionId = expressionId,
                            codeDelta = event.chunk
                        ))
                    }
                    null -> IllegalStateException( // should never happen
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
                            id = expressionId,
                            expressionId = expressionId
                        ))
                    }
                    null -> IllegalStateException( // should never happen
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
