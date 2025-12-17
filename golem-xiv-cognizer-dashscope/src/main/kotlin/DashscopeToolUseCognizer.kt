/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.cognizer.dashscope

import com.alibaba.dashscope.aigc.generation.Generation
import com.alibaba.dashscope.aigc.generation.GenerationParam
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.MessageContentText
import com.alibaba.dashscope.common.Role
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.backend.Cognizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.reactive.asFlow

/**
 * Alibaba Dashscope can access Qwen models.
 */
class DashscopeToolUseCognizer(
    private val generation: Generation,
) : Cognizer {

//    // TODO convert from anthropic to GSON tool schema
//    private val tools = golemTools.map {
//
//    }


    override fun reason(
        constitution: List<String>,
        cognitionId: Long,
        phenomenalFlow: List<PhenomenalExpression>,
        hints: Map<String, String>
    ): Flow<CognitionEvent> {
        val system = constitution.toDashscopeMessage(Role.SYSTEM)
        val messages = phenomenalFlow.map { it.toDashscopeMessage() }
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
): Message = Message.builder()
    .role(role.value)
    .contents(toDashscopeContents())
    .build()

private fun PhenomenalExpression.toDashscopeMessage(): Message = Message.builder()
    .role(agent.toDashscopeRole().value)
    //.contents(phenomena.filterIsInstance<Text>().map { it.text }.toDashscopeContents())
    .build()

private fun EpistemicAgent.toDashscopeRole() = when (this) {
    is EpistemicAgent.AI -> Role.ASSISTANT
    else -> Role.USER
}
