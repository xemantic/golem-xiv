/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.cognizer.anthropic

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.content.Text
import com.xemantic.ai.anthropic.content.ToolResult
import com.xemantic.ai.anthropic.content.ToolUse
import com.xemantic.ai.anthropic.event.Event
import com.xemantic.ai.anthropic.message.Message
import com.xemantic.ai.anthropic.message.Role
import com.xemantic.ai.anthropic.message.System
import com.xemantic.ai.anthropic.message.addCacheBreakpoint
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.backend.Cognizer2
import com.xemantic.ai.golem.api.backend.CognizerEvent
import com.xemantic.ai.golem.api.backend.CognizerEvent.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class AnthropicCognizer(
    private val anthropic: Anthropic
) : Cognizer2 {

    private val logger = KotlinLogging.logger {}

    override fun reason(
        constitution: List<String>,
        cognitionId: Long,
        phenomenalFlow: List<PhenomenalExpression>,
        hints: Map<String, String>
    ): Flow<CognizerEvent> {

        logger.debug {
            "Cognition[$cognitionId]: reasoning"
        }

        val messageFlow = phenomenalFlow.toAnthropicMessages().addCacheBreakpoint()

        logger.trace {
            "Cognition[$cognitionId]: anthropic messages: $messageFlow"
        }

        val flow = anthropic.messages.stream {
            system = constitution.toAnthropicSystem()
            messages = messageFlow
        }.transform { event ->
            when (event) {

                is Event.MessageStart -> {
                    logger.debug { "Cognition[$cognitionId]: MessageStart event" }
                }

                is Event.ContentBlockStart -> {
                    when (event.contentBlock) {

                        is Event.ContentBlockStart.ContentBlock.Text -> {
                            emit(CognizerEvent.TextStart)
                        }

                        is Event.ContentBlockStart.ContentBlock.ToolUse -> {
                            logger.error {
                                "Tool use should never happen with this cognizer: $event"
                            }
                        }

                    }
                }

                is Event.ContentBlockDelta -> {

                    when (event.delta) {

                        is Event.ContentBlockDelta.Delta.TextDelta -> {
                            val textDelta = (event.delta as Event.ContentBlockDelta.Delta.TextDelta).text
                            emit(TextDelta(textDelta))
                        }

                        is Event.ContentBlockDelta.Delta.InputJsonDelta -> {
                            logger.error {
                                "InputJsonDelta should never happen with this cognizer: $event"
                            }
                        }
                    }
                }

                is Event.ContentBlockStop -> {
                    emit(CognizerEvent.TextEnd)
                }

                is Event.MessageDelta -> {
                    emit(Usage(categories = emptyMap()))
                }

                is Event.MessageStop -> {


                }

                is Event.Error -> {
                    logger.error { "Cognition[$cognitionId]: Error, ${event.error}" }
                }


                is Event.Ping -> {
                    logger.trace { "Cognition[$cognitionId]: Ping event" }
                }
            }
        }.onStart {
            logger.debug { "Cognition[$cognitionId]: API streaming start" }
        }.onCompletion {
            logger.debug { "Cognition[$cognitionId]: API streaming stop" }
        }

        return flow
    }

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
        isError = impeded
    }
    else -> throw IllegalStateException("Unsupported content type: $this")
}

private fun List<Phenomenon>.toAnthropicContent() = map { it.toAnthropicContent() }

private fun PhenomenalExpression.toAnthropicMessage() = Message {
    role = agent.toAnthropicRole()
    content = phenomena.toAnthropicContent()
}

private fun EpistemicAgent.toAnthropicRole() = when (this) {
    is EpistemicAgent.AI -> Role.ASSISTANT
    else -> Role.USER
}

private fun List<PhenomenalExpression>.toAnthropicMessages() = map {
    it.toAnthropicMessage()
}

private fun List<String>.toAnthropicSystem() = map {
    System(text = it)
}
