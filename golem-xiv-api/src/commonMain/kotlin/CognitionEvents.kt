/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

@file:UseSerializers(InstantSerializer::class)

package com.xemantic.ai.golem.api

import com.xemantic.ai.golem.serialization.time.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

/**
 * Cognition events are "broadcasted" from the cognitive process unfolding
 * over provided [CognitiveWorkspace].
 */
@Serializable
sealed interface CognitionEvent {

    @Serializable
    @SerialName("expressionInitiation")
    data class ExpressionInitiation(
        val expressionId: Long,
        val agent: EpistemicAgent,
        val moment: Instant
    ) : CognitionEvent

    @Serializable
    @SerialName("expressionCulmination")
    data class ExpressionCulmination(
        val expressionId: Long,
        val moment: Instant
    ) : CognitionEvent // TODO add message metadata, like usage tax

    @Serializable
    @SerialName("TextInitiation")
    data class TextInitiation(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("textUnfolding")
    data class TextUnfolding(
        val id: Long,
        val expressionId: Long,
        val textDelta: String
    ) : CognitionEvent

    @Serializable
    @SerialName("textCulmination")
    data class TextCulmination(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("intentInitiation")
    data class IntentInitiation(
        val id: Long,
        val expressionId: Long,
        val systemId: String,
        val recursiveWorkspaceId: String? = null
    ) : CognitionEvent

    @Serializable
    @SerialName("intentPurposeInitiation")
    data class IntentPurposeInitiation(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("intentPurposeUnfolding")
    data class IntentPurposeUnfolding(
        val id: Long,
        val expressionId: Long,
        val purposeDelta: String,
    ) : CognitionEvent

    @Serializable
    @SerialName("intentPurposeCulmination")
    data class IntentPurposeCulmination(
        val id: Long,
        val expressionId: Long,
    ) : CognitionEvent

    @Serializable
    @SerialName("intentCodeInitiation")
    data class IntentCodeInitiation(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("intentCodeUnfolding")
    data class IntentCodeUnfolding(
        val id: Long,
        val expressionId: Long,
        val codeDelta: String,
    ) : CognitionEvent

    @Serializable
    @SerialName("intentCodeCulmination")
    data class IntentCodeCulmination(
        val id: Long,
        val expressionId: Long,
    ) : CognitionEvent

    @Serializable
    @SerialName("intentCulmination")
    data class IntentCulmination(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("fulfillmentStart")
    data class FulfillmentInitiation(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("fulfillmentUnfolding")
    data class FulfillmentUnfolding(
        val id: Long,
        val expressionId: Long,
        val designation: String // TODO why designation?
    ) : CognitionEvent

    @Serializable
    @SerialName("fulfillmentCulmination")
    data class FulfillmentCulmination(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent
    // TODO here we need impediment indication

    @Serializable
    @SerialName("recursiveFulfillmentUnfolding")
    data class RecursiveFulfillmentUnfolding(
        val id: Long,
        val expressionId: Long,
        val recursiveWorkspaceId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("workspaceDesignation")
    data class WorkspaceDesignation(
        val expressionId: Long,
        val title: String
    ) : CognitionEvent

//    @Serializable
//    @SerialName("recursiveCognition")
//    data class RecursiveCognition(
//        val id: Uuid
//    )

}
