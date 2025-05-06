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

@Serializable
@JsonClassDiscriminator("type")
sealed class GolemInput {

    abstract val contextId: Uuid

    @Serializable
    @SerialName("test")
    data class Test(
        override val contextId: Uuid,
        val text: String
    ) : GolemInput()

    @Serializable
    @SerialName("prompt")
    data class Prompt(
        override val contextId: Uuid,
        val message: Message
    ) : GolemInput()

}
