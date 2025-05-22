/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

interface WithWorkspaceId {

    val workspaceId: String

}

@Serializable
@JsonClassDiscriminator("type")
sealed class GolemOutput {

    @Serializable
    @SerialName("welcome")
    data class Welcome (
        val message: String
    ): GolemOutput()

    @Serializable
    @SerialName("message")
    data class Message (
        override val workspaceId: String,
        val expression: Expression
    ): GolemOutput(), WithWorkspaceId

    @Serializable
    @SerialName("textDelta")
    data class TextDelta (
        override val workspaceId: String,
        val delta: String
    ): GolemOutput(), WithWorkspaceId

    @Serializable
    @SerialName("osProcess")
    data class OsProcess(
        override val workspaceId: String,
        val event: OsProcessEvent
    ) : GolemOutput(), WithWorkspaceId

    @Serializable
    @SerialName("workspaceAdded")
    data class WorkspaceAdded(
        override val workspaceId: String
    ) : GolemOutput(), WithWorkspaceId

    @Serializable
    @SerialName("workspaceUpdated")
    data class WorkspaceUpdated(
        override val workspaceId: String,
//        val info: CognitiveWorkspaceInfo
    ) : GolemOutput(), WithWorkspaceId

    @Serializable
    @SerialName("reasoning")
    data class Cognition( // TODO cognizing?
        override val workspaceId: String,
        val event: CognitionEvent
    ) : GolemOutput(), WithWorkspaceId

}
