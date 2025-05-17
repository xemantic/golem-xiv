/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.cognition.dashscope

import com.alibaba.dashscope.aigc.generation.Generation
import com.alibaba.dashscope.aigc.generation.GenerationParam
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.MessageContentText
import com.alibaba.dashscope.common.Role
import com.xemantic.ai.anthropic.tool.Tool
import com.xemantic.ai.golem.api.Agent
import com.xemantic.ai.golem.api.Expression
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.server.cognition.Cognizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.reactive.asFlow
import kotlin.uuid.Uuid

/**
 * Alibaba Dashscope can access Qwen models.
 */
class DashscopeCognizer(
    private val generation: Generation,
    private val golemTools: List<Tool>
) : Cognizer {

    // TODO convert from anthropic to GSON tool schema
    private val tools = golemTools.map {

    }

    override fun reason(
        system: List<String>,
        conversation: List<Expression>,
        hints: Map<String, String>
    ): Flow<CognitionEvent> {
        val expressionId = Uuid.random().toString()
        val system = system.toDashscopeMessage(Role.SYSTEM)
        val messages = conversation.map { it.toDashscopeMessage() }
        val fullMessages = buildList {
            add(system)
            addAll(messages)
        }
        val param = GenerationParam.builder()
            .apiKey(System.getenv("DASHSCOPE_API_KEY")) // This example uses qwen-plus. You can change the model name as needed. For the model list, visit: https://www.alibabacloud.com/help/en/model-studio/getting-started/models
            .model("qwen-plus-2025-04-28")
            .enableThinking(true)
            .thinkingBudget(38912)
            .incrementalOutput(true)
            .messages(fullMessages)
            .resultFormat(GenerationParam.ResultFormat.MESSAGE)
            .build()

        var aggregatedMessage = ""
        return generation.streamCall(param).asFlow().transform {generationResult ->
            if (aggregatedMessage.isEmpty()) {
                //emit(CognitionEvent.ExpressionStart(messageId, role = Expression.Role.ASSISTANT))
                //emit(CognitionEvent.TextStart(messageId))
            }
            val content = generationResult.output.choices[0].message.content
            val chunk = content.substringAfter(aggregatedMessage)
            aggregatedMessage += chunk
            //emit(CognitionEvent.TextDelta(messageId, chunk))

            println(chunk)
            //println(generationResult.output.choices[0].message.contents)
//            emit(ReasoningEvent.MessageStart(Uuid.random(), role = Message.Role.ASSISTANT))
        }
    }

}

private fun List<String>.toDashscopeContents(): List<MessageContentText> = map {
    MessageContentText.builder().text(it).build()
}

private fun List<String>.toDashscopeMessage(
    role: Role
): com.alibaba.dashscope.common.Message = com.alibaba.dashscope.common.Message.builder()
    .role(role.value)
    .contents(toDashscopeContents())
    .build()

private fun Expression.toDashscopeMessage(): Message = Message.builder()
    .role(agent.toDashscopeRole().value)
    //.contents(phenomena.filterIsInstance<Text>().map { it.text }.toDashscopeContents())
    .build()

private fun Agent.toDashscopeRole(): Role = when (this.category) {
    Agent.Category.SELF -> Role.USER
    else  -> Role.ASSISTANT
}
