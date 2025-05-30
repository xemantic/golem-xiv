/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.backend.api.util

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.backend.api.CognitiveWorkspaceRepository
import com.xemantic.ai.golem.backend.api.Cognizer
import com.xemantic.ai.golem.json.DefaultStreamingJsonParser
import com.xemantic.ai.golem.json.JsonEvent

/**
 * The intent cognizer is a component to be used in the implementation of [Cognizer] interface.
 */
class IntentCognizer(
    private val workspaceId: Long,
    private val expressionId: Long,
    private val phenomenonId: Long,
    private val repository: CognitiveWorkspaceRepository
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
                            workspaceId = workspaceId,
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
                            workspaceId = workspaceId,
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
