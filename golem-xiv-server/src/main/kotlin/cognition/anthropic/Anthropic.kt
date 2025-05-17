/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.cognition.anthropic

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.cache.CacheControl
import com.xemantic.ai.anthropic.content.ToolUse
import com.xemantic.ai.anthropic.event.Event
import com.xemantic.ai.anthropic.message.Message
import com.xemantic.ai.anthropic.message.Role
import com.xemantic.ai.anthropic.message.System
import com.xemantic.ai.anthropic.message.addCacheBreakpoint
import com.xemantic.ai.anthropic.tool.Tool
import com.xemantic.ai.golem.api.Agent
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.Expression
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.server.cognition.Cognizer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal fun Phenomenon.toAnthropicContent() = when (this) {
    is Phenomenon.Text -> toAnthropicText()
    //is Phenomenon.Intent ->
    is Phenomenon.Fulfillment -> toAnthropicToolResult()
    is Phenomenon.Impediment -> toAnthropicToolResult()
    else -> throw IllegalStateException("Unsupported content type")
}

internal fun List<Phenomenon>.toAnthropicContent() = map { it.toAnthropicContent() }

internal fun Expression.toAnthropicMessage() = Message {
    role = agent.toAnthropicRole()
    content = phenomena.toAnthropicContent()
}

internal fun Agent.toAnthropicRole() = when (this.category) {
    Agent.Category.SELF -> Role.ASSISTANT
    else -> Role.USER
}

internal fun List<Expression>.toAnthropicMessages() = map {
    it.toAnthropicMessage()
}

fun Phenomenon.Text.toAnthropicText() = com.xemantic.ai.anthropic.content.Text(text)

fun Phenomenon.Fulfillment.toAnthropicToolResult() = com.xemantic.ai.anthropic.content.ToolResult {
    toolUseId = intentSystemId
    content = listOf(com.xemantic.ai.anthropic.content.Text(result))
}

fun Phenomenon.Impediment.toAnthropicToolResult() = com.xemantic.ai.anthropic.content.ToolResult {
    toolUseId = intentSystemId
    content = listOf(com.xemantic.ai.anthropic.content.Text(reason))
    isError = true
}

//fun Phenomenon.Intent.toAnthropic() = ToolUse {
//    id = systemId,
//    input = J
//
//}

fun List<String>.toAnthropicSystem() = map {
    System(text = it, cacheControl = CacheControl.Ephemeral())
}

class AnthropicCognizer(
    private val anthropic: Anthropic,
    private val golemTools: List<Tool>
) : Cognizer {

    private val logger = KotlinLogging.logger {}

    override fun reason(
        system: List<String>,
        phenomenalFlow: List<Expression>,
        hints: Map<String, String>
    ): Flow<CognitionEvent> {

        logger.debug { "Anthropic API: Streaming start" }

        val expressionId = Uuid.random().toString()

        var processedContentType: ProcessedContentType? = null

        val flow = anthropic.messages.stream {
            this.system = system.toAnthropicSystem()
            messages = phenomenalFlow.toAnthropicMessages().addCacheBreakpoint()
            tools = golemTools
        }.transform { event ->
            when (event) {
                is Event.MessageStart -> {
                    CognitionEvent.ExpressionInitiation(
                        expressionId,
                        agent = Agent(
                            id = "golem",
                            description = "The agent",
                            category = Agent.Category.SELF
                        ),
                        moment = Clock.System.now()
                    )
                }
                is Event.ContentBlockStart -> {
                    when (event.contentBlock) {
                        is Event.ContentBlockStart.ContentBlock.Text -> {
                            processedContentType = ProcessedContentType.TEXT
                            CognitionEvent.TextInitiation(expressionId)
                        }
                        is Event.ContentBlockStart.ContentBlock.ToolUse -> {
                            processedContentType = ProcessedContentType.TOOL
                            CognitionEvent.IntentInitiation(
                                expressionId,
                                purpose = "TODO internal parsing",
                                systemId = (event.contentBlock as Event.ContentBlockStart.ContentBlock.ToolUse).id
                            )
                        }
                    }
                }
                is Event.ContentBlockDelta -> {
                    when (event.delta) {
                        is Event.ContentBlockDelta.Delta.TextDelta -> {
                            CognitionEvent.TextUnfolding(
                                expressionId,
                                textDelta = (event.delta as Event.ContentBlockDelta.Delta.TextDelta).text
                            )
                        }
                        is Event.ContentBlockDelta.Delta.InputJsonDelta -> {
                            CognitionEvent.IntentUnfolding(
                                expressionId,
                                instructionsDelta = (event.delta as Event.ContentBlockDelta.Delta.InputJsonDelta).partialJson
                            )
                        }
                    }
                }
                is Event.ContentBlockStop -> {
                    when (processedContentType!!) {
                        ProcessedContentType.TEXT -> {
                            CognitionEvent.TextCulmination(expressionId)
                        }
                        ProcessedContentType.TOOL -> {
                            CognitionEvent.IntentCulmination(expressionId)
                        }
                    }.also {
                        processedContentType = null
                    }
                }
                is Event.MessageStop -> CognitionEvent.ExpressionCulmination(
                    expressionId,
                    moment = Clock.System.now()
                )
                else -> null
            }?.let {
                emit(it)
            }
        }

        logger.debug { "Anthropic API: Streaming finished" }

        return flow
    }

}

private enum class ProcessedContentType {
    TEXT,
    TOOL
}
