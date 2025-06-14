/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface WithWorkspaceId {

    val workspaceId: Long

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
        override val workspaceId: Long,
        val expression: PhenomenalExpression
    ): GolemOutput, WithWorkspaceId

    @Serializable
    @SerialName("WorkspaceAdded")
    data class WorkspaceAdded(
        override val workspaceId: Long
    ) : GolemOutput, WithWorkspaceId

    @Serializable
    @SerialName("WorkspaceUpdated")
    data class WorkspaceUpdated(
        override val workspaceId: Long,
//        val info: CognitiveWorkspaceInfo
    ) : GolemOutput, WithWorkspaceId

    @Serializable
    @SerialName("Cognition")
    data class Cognition(
        override val workspaceId: Long,
        val event: CognitionEvent
    ) : GolemOutput, WithWorkspaceId

}
