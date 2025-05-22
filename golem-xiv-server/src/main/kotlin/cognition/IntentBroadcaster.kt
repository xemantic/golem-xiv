/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.cognition

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.server.kotlin.DefaultStreamingJsonParser
import com.xemantic.ai.golem.server.kotlin.JsonEvent

class IntentBroadcaster(
    private val expressionId: String
) {

    private val jsonParser = DefaultStreamingJsonParser()

    private var phase: Phase? = null

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
                        add(CognitionEvent.IntentPurposeInitiation(expressionId))
                    }
                    "code" -> {
                        phase = Phase.COLLECTING_CODE
                        add(CognitionEvent.IntentCodeInitiation(expressionId))
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
                        add(CognitionEvent.IntentPurposeUnfolding(expressionId, purposeDelta = event.chunk))
                    }
                    Phase.COLLECTING_CODE -> {
                        add(CognitionEvent.IntentCodeUnfolding(expressionId, codeDelta = event.chunk))
                    }
                    null -> IllegalStateException( // should never happen
                        "No IntentAccumulator.phase selected, but received JsonEven.StringDelta: $chunk"
                    )
                }
            }
            is JsonEvent.StringEnd -> {
                when (phase) {
                    Phase.COLLECTING_PURPOSE -> {
                        add(CognitionEvent.IntentPurposeCulmination(expressionId))
                    }
                    Phase.COLLECTING_CODE -> {
                        add(CognitionEvent.IntentCodeCulmination(expressionId))
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
