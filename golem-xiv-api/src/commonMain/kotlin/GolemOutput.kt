/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface WithCognitionId {

    val cognitionId: Long

}

@Serializable
sealed interface GolemOutput {

    @Serializable
    @SerialName("Welcome")
    data class Welcome (
        val message: String
    ): GolemOutput

    @Serializable
    @SerialName("Message")
    data class Message (
        override val cognitionId: Long,
        val expression: PhenomenalExpression
    ): GolemOutput, WithCognitionId

    @Serializable
    @SerialName("CognitionAdded")
    data class CognitionAdded(
        override val cognitionId: Long
    ) : GolemOutput, WithCognitionId

    @Serializable
    @SerialName("CognitionUpdated")
    data class CognitionUpdated(
        override val cognitionId: Long,
//        val info: CognitionInfo
    ) : GolemOutput, WithCognitionId

    @Serializable
    @SerialName("Cognition")
    data class Cognition(
        override val cognitionId: Long,
        val event: CognitionEvent
    ) : GolemOutput, WithCognitionId

}
