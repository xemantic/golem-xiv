/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

@file:UseSerializers(InstantIso8601Serializer::class)

package com.xemantic.ai.golem.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

/**
 * Cognition events are "broadcasted" from the cognitive process unfolding
 * over provided [CognitiveWorkspace]
 */
@Serializable
sealed interface CognitionEvent {

    @Serializable
    @SerialName("expressionInitiation")
    data class ExpressionInitiation(
        val expressionId: String,
        val agent: Agent,
        val moment: Instant
    ) : CognitionEvent

    @Serializable
    @SerialName("expressionCulmination")
    data class ExpressionCulmination(
        val expressionId: String,
        val moment: Instant
    ) : CognitionEvent // TODO add message metadata, like usage tax

    @Serializable
    @SerialName("TextInitiation")
    data class TextInitiation(
        val expressionId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("textUnfolding")
    data class TextUnfolding(
        val expressionId: String,
        val textDelta: String
    ) : CognitionEvent

    @Serializable
    @SerialName("textCulmination")
    data class TextCulmination(
        val expressionId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("intentInitiation")
    data class IntentInitiation(
        val expressionId: String,
        val systemId: String,
        val recursiveWorkspaceId: String? = null
    ) : CognitionEvent

    @Serializable
    @SerialName("intentPurposeInitiation")
    data class IntentPurposeInitiation(
        val expressionId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("intentPurposeUnfolding")
    data class IntentPurposeUnfolding(
        val expressionId: String,
        val purposeDelta: String,
    ) : CognitionEvent

    @Serializable
    @SerialName("intentPurposeCulmination")
    data class IntentPurposeCulmination(
        val expressionId: String,
    ) : CognitionEvent

    @Serializable
    @SerialName("intentCodeInitiation")
    data class IntentCodeInitiation(
        val expressionId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("intentCodeUnfolding")
    data class IntentCodeUnfolding(
        val expressionId: String,
        val codeDelta: String,
    ) : CognitionEvent

    @Serializable
    @SerialName("intentCodeCulmination")
    data class IntentCodeCulmination(
        val expressionId: String,
    ) : CognitionEvent

    @Serializable
    @SerialName("intentCulmination")
    data class IntentCulmination(
        val expressionId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("fulfillmentStart")
    data class FulfillmentInitiation(
        val expressionId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("fulfillmentUnfolding")
    data class FulfillmentUnfolding(
        val expressionId: String,
        val designation: String // TODO why designation?
    ) : CognitionEvent

    @Serializable
    @SerialName("fulfillmentCulmination")
    data class FulfillmentCulmination(
        val expressionId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("RecursiveFulfillmentUnfolding")
    data class RecursiveFulfillmentUnfolding(
        val expressionId: String,
        val recursiveWorkspaceId: String
    ) : CognitionEvent



    @Serializable
    @SerialName("workspaceDesignation")
    data class WorkspaceDesignation(
        val expressionId: String,
        val title: String
    ) : CognitionEvent

//    @Serializable
//    @SerialName("recursiveCognition")
//    data class RecursiveCognition(
//        val id: Uuid
//    )

}
