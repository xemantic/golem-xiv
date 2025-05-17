/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.cognition

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.tool.Tool
import com.xemantic.ai.golem.api.Expression
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.server.cognition.anthropic.AnthropicCognizer
import kotlinx.coroutines.flow.Flow

interface Cognizer {

    fun reason(
        system: List<String>,
        phenomenalFlow: List<Expression>,
        hints: Map<String, String>
    ): Flow<CognitionEvent>

}

//private val defaultCognizer = DashscopeCognizer(
//    Generation(Protocol.HTTP.value, "https://dashscope-intl.aliyuncs.com/api/v1")
//)

fun cognizer(tools: List<Tool>): Cognizer = AnthropicCognizer(
    Anthropic(),
    golemTools = tools
)
