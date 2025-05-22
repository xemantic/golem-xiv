/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.cognition

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.tool.Tool
import com.xemantic.ai.golem.server.script.ExecuteGolemScript
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AnthropicCognizerTest {

    @Test
    fun `should reason to create kotlin script`() = runTest {
        val anthropic = Anthropic {
            logHttp = true
        }
        val tool = Tool<ExecuteGolemScript> {
        }
//        val cognizer = AnthropicCognizer(anthropic, listOf(tool))
        anthropic.messages.stream {
            +"Write me a kotlin script with long poem"
            tools = listOf(tool)
        }.collect {
            println(it)
        }
    }

}
