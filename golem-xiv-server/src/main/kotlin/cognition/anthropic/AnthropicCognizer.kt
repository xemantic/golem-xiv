/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.cognition.anthropic

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.anthropic.content.ToolResult
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
import com.xemantic.ai.golem.server.cognition.IntentBroadcaster
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Clock
import kotlin.uuid.Uuid

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

        logger.debug { "Reasoning" }

        val expressionId = Uuid.random().toString()

        val intentBroadcaster = IntentBroadcaster(expressionId)

        var processedContentType: ProcessedContentType? = null

        var toolUseId: String? = null

        val flow = anthropic.messages.stream {
            this.system = system.toAnthropicSystem()
            messages = phenomenalFlow.toAnthropicMessages().addCacheBreakpoint()
            tools = golemTools
        }.transform { event ->
            logger.debug { "In: $event" }
            when (event) {
                is Event.MessageStart -> {
                    emit(
                        CognitionEvent.ExpressionInitiation(
                            expressionId,
                            agent = Agent( // TODO better agent description and model spec
                                id = "golem",
                                description = "The agent",
                                category = Agent.Category.SELF
                            ),
                            moment = Clock.System.now()
                        )
                    )
                }
                is Event.ContentBlockStart -> {
                    when (event.contentBlock) {
                        is Event.ContentBlockStart.ContentBlock.Text -> {
                            processedContentType = ProcessedContentType.TEXT
                            emit(CognitionEvent.TextInitiation(expressionId))
                        }
                        is Event.ContentBlockStart.ContentBlock.ToolUse -> {
                            processedContentType = ProcessedContentType.TOOL
                            toolUseId = (event.contentBlock as Event.ContentBlockStart.ContentBlock.ToolUse).id
                            emit(CognitionEvent.IntentInitiation(expressionId, systemId = toolUseId))
                            // we have only one tool, so we don't even check the name
                            // no emission
                        }
                    }
                }
                is Event.ContentBlockDelta -> {
                    when (event.delta) {
                        is Event.ContentBlockDelta.Delta.TextDelta -> {
                            val textDelta = (event.delta as Event.ContentBlockDelta.Delta.TextDelta).text
                            emit(
                                CognitionEvent.TextUnfolding(
                                    expressionId,
                                    textDelta
                                )
                            )
                        }
                        is Event.ContentBlockDelta.Delta.InputJsonDelta -> {
                            val jsonDelta = (event.delta as Event.ContentBlockDelta.Delta.InputJsonDelta).partialJson
                            intentBroadcaster.add(jsonDelta).forEach { result ->
                                logger.debug { "Co: $result" }
                                emit(result)
                            }
                        }
                    }
                }
                is Event.ContentBlockStop -> {
                    when (processedContentType!!) {
                        ProcessedContentType.TEXT -> {
                            emit(CognitionEvent.TextCulmination(expressionId))
                        }
                        ProcessedContentType.TOOL -> {
                            emit(CognitionEvent.IntentCulmination(expressionId))
                        }
                    }
                    processedContentType = null
                }
                is Event.MessageStop -> emit(
                    CognitionEvent.ExpressionCulmination(
                        expressionId,
                        moment = Clock.System.now()
                    )
                )
                else -> null
            }
        }.onStart {
            logger.debug { "API streaming: start" }
        }.onCompletion {
            logger.debug { "API streaming: stop" }
        }

        return flow
    }

}

private enum class ProcessedContentType {
    TEXT,
    TOOL
}

private fun Phenomenon.toAnthropicContent() = when (this) {
    is Phenomenon.Text -> Text(text)
    is Phenomenon.Intent -> ToolUse {
        id = systemId
        name = "GolemScriptExecutor"
        input = buildJsonObject {
            put("purpose", JsonPrimitive(purpose))
            put("code", JsonPrimitive(code))
        }
    }
    is Phenomenon.Fulfillment -> ToolResult {
        toolUseId = intentSystemId
        content = listOf(Text(result))
    }
    is Phenomenon.Impediment -> ToolResult {
        toolUseId = intentSystemId
        content = listOf(Text(reason))
        isError = true
    }
    else -> throw IllegalStateException("Unsupported content type: $this")
}

private fun List<Phenomenon>.toAnthropicContent() = map { it.toAnthropicContent() }

private fun Expression.toAnthropicMessage() = Message {
    role = agent.toAnthropicRole()
    content = phenomena.toAnthropicContent()
}

private fun Agent.toAnthropicRole() = when (this.category) {
    Agent.Category.SELF -> Role.ASSISTANT
    else -> Role.USER
}

private fun List<Expression>.toAnthropicMessages() = map {
    it.toAnthropicMessage()
}

private fun List<String>.toAnthropicSystem() = map {
    System(text = it)
}
