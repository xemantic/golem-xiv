/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.client

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.PhenomenalExpression
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface WithWorkspaceId {

    val workspaceId: Long

}

@Serializable
sealed class GolemOutput {

    @Serializable
    @SerialName("welcome")
    data class Welcome (
        val message: String
    ): GolemOutput()

    @Serializable
    @SerialName("message")
    data class Message (
        override val workspaceId: Long,
        val expression: PhenomenalExpression
    ): GolemOutput(), WithWorkspaceId

    @Serializable
    @SerialName("workspaceAdded")
    data class WorkspaceAdded(
        override val workspaceId: Long
    ) : GolemOutput(), WithWorkspaceId

    @Serializable
    @SerialName("workspaceUpdated")
    data class WorkspaceUpdated(
        override val workspaceId: Long,
//        val info: CognitiveWorkspaceInfo
    ) : GolemOutput(), WithWorkspaceId

    @Serializable
    @SerialName("reasoning")
    data class Cognition( // TODO cognizing?
        override val workspaceId: Long,
        val event: CognitionEvent
    ) : GolemOutput(), WithWorkspaceId

}
