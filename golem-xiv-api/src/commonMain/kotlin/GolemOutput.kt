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
import kotlin.uuid.Uuid

interface WithContextId {

    val contextId: Uuid

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
        override val contextId: Uuid,
        val message: com.xemantic.ai.golem.api.Message
    ): GolemOutput(), WithContextId

    @Serializable
    @SerialName("textDelta")
    data class TextDelta (
        override val contextId: Uuid,
        val delta: String
    ): GolemOutput(), WithContextId

    @Serializable
    @SerialName("osProcess")
    data class OsProcess(
        override val contextId: Uuid,
        val event: OsProcessEvent
    ) : GolemOutput(), WithContextId

    @Serializable
    @SerialName("contextAdded")
    data class ContextAdded(
        override val contextId: Uuid
    ) : GolemOutput(), WithContextId

    @Serializable
    @SerialName("contextUpdated")
    data class ContextUpdated(
        override val contextId: Uuid,
        val info: ContextInfo
    ) : GolemOutput(), WithContextId

    @Serializable
    @SerialName("reasoning")
    data class Reasoning(
        override val contextId: Uuid,
        val event: ReasoningEvent
    ) : GolemOutput(), WithContextId

}
