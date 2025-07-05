/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Cognition events are "broadcasted" from the unfolding cognitive process.
 */
@Serializable
sealed interface CognitionEvent {

    @Serializable
    @SerialName("ExpressionInitiation")
    data class ExpressionInitiation(
        val expressionId: Long,
        val agent: EpistemicAgent,
        val moment: Instant
    ) : CognitionEvent

    @Serializable
    @SerialName("ExpressionCulmination")
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
    @SerialName("TextUnfolding")
    data class TextUnfolding(
        val id: Long,
        val expressionId: Long,
        val textDelta: String
    ) : CognitionEvent

    @Serializable
    @SerialName("TextCulmination")
    data class TextCulmination(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("IntentInitiation")
    data class IntentInitiation(
        val id: Long,
        val expressionId: Long,
        val systemId: String,
        val recursiveCognitionId: String? = null
    ) : CognitionEvent

    @Serializable
    @SerialName("IntentPurposeInitiation")
    data class IntentPurposeInitiation(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("IntentPurposeUnfolding")
    data class IntentPurposeUnfolding(
        val id: Long,
        val expressionId: Long,
        val purposeDelta: String,
    ) : CognitionEvent

    @Serializable
    @SerialName("IntentPurposeCulmination")
    data class IntentPurposeCulmination(
        val id: Long,
        val expressionId: Long,
    ) : CognitionEvent

    @Serializable
    @SerialName("IntentCodeInitiation")
    data class IntentCodeInitiation(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("IntentCodeUnfolding")
    data class IntentCodeUnfolding(
        val id: Long,
        val expressionId: Long,
        val codeDelta: String,
    ) : CognitionEvent

    @Serializable
    @SerialName("IntentCodeCulmination")
    data class IntentCodeCulmination(
        val id: Long,
        val expressionId: Long,
    ) : CognitionEvent

    @Serializable
    @SerialName("IntentCulmination")
    data class IntentCulmination(
        val id: Long,
        val expressionId: Long
    ) : CognitionEvent

    @Serializable
    @SerialName("FulfillmentInitiation")
    data class FulfillmentInitiation(
        val id: Long,
        val expressionId: Long,
        val intentId: Long,
        val intentSystemId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("FulfillmentUnfolding")
    data class FulfillmentUnfolding(
        val id: Long,
        val expressionId: Long,
        val textDelta: String
    ) : CognitionEvent

    @Serializable
    @SerialName("FulfillmentCulmination")
    data class FulfillmentCulmination(
        val id: Long,
        val expressionId: Long,
        val impeded: Boolean
    ) : CognitionEvent

    @Serializable
    @SerialName("RecursiveFulfillmentUnfolding")
    data class RecursiveFulfillmentUnfolding(
        val id: Long,
        val expressionId: Long,
        val recursiveCognitionId: String
    ) : CognitionEvent

    @Serializable
    @SerialName("CognitionDesignation")
    data class CognitionDesignation(
        val expressionId: Long,
        val title: String
    ) : CognitionEvent

//    @Serializable
//    @SerialName("recursiveCognition")
//    data class RecursiveCognition(
//        val id: Uuid
//    )

}
