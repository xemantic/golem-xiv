/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.cognition

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.golem.api.Message
import com.xemantic.ai.golem.api.ReasoningEvent
import com.xemantic.ai.golem.server.cognition.anthropic.AnthropicCognizer
import kotlinx.coroutines.flow.Flow

interface Cognizer {

    fun reason(
        system: List<String>,
        conversation: List<Message>,
        hints: Map<String, String>
    ): Flow<ReasoningEvent>

}

private val defaultCognizer = AnthropicCognizer(
    Anthropic()
)

//private val defaultCognizer = DashscopeCognizer(
//    Generation(Protocol.HTTP.value, "https://dashscope-intl.aliyuncs.com/api/v1")
//)

fun cognizer(): Cognizer = defaultCognizer
