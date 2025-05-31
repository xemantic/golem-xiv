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
import com.xemantic.ai.anthropic.tool.Tool
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceRepository
import com.xemantic.ai.golem.api.backend.Cognizer
import com.xemantic.ai.golem.api.backend.script.ExecuteGolemScript
import com.xemantic.ai.golem.api.backend.util.IntentCognizer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class AnthropicToolUseCognizer(
    private val anthropic: Anthropic,
    private val golemSelfId: Long,
    private val repository: CognitiveWorkspaceRepository
) : Cognizer {

    private val logger = KotlinLogging.logger {}

    private val golemTools = listOf(Tool<ExecuteGolemScript>())

    override fun reason(
        conditioning: List<String>,
        workspaceId: Long,
        phenomenalFlow: List<PhenomenalExpression>,
        hints: Map<String, String>
    ): Flow<CognitionEvent> {

        logger.debug { "Reasoning" }

        var expressionId: Long? = null

        var phenomenonId: Long? = null

        var intentCognizer: IntentCognizer? = null

        var processedContentType: ProcessedContentType? = null

        var toolUseId: String? = null

        val messageFlow = phenomenalFlow.toAnthropicMessages().addCacheBreakpoint()

        logger.trace {
            "Anthropic messages: $messageFlow"
        }

        val flow = anthropic.messages.stream {
            system = conditioning.toAnthropicSystem()
            messages = messageFlow
            tools = golemTools
        }.transform { event ->
            when (event) {

                is Event.MessageStart -> {

                    val agent = EpistemicAgent.AI(
                        id = golemSelfId,
                        model = event.message.model,
                        vendor = "Anthropic"
                    )

                    val info = repository.initiateExpression(workspaceId, agent)
                    expressionId = info.id

                    emit(
                        CognitionEvent.ExpressionInitiation(
                            expressionId = info.id,
                            agent = EpistemicAgent.AI(
                                id = golemSelfId,
                                model = event.message.model,
                                vendor = "Anthropic"
                            ),
                            moment = info.initiationMoment
                        )
                    )
                }

                is Event.ContentBlockStart -> {

                    when (event.contentBlock) {

                        is Event.ContentBlockStart.ContentBlock.Text -> {

                            processedContentType = ProcessedContentType.TEXT

                            val id = repository.initiateTextPhenomenon(
                                workspaceId = workspaceId,
                                expressionId = expressionId!!,
                            )

                            phenomenonId = id

                            emit(CognitionEvent.TextInitiation(
                                id = id,
                                expressionId = expressionId
                            ))
                        }

                        is Event.ContentBlockStart.ContentBlock.ToolUse -> {

                            processedContentType = ProcessedContentType.TOOL
                            toolUseId = (event.contentBlock as Event.ContentBlockStart.ContentBlock.ToolUse).id

                            val id = repository.initiateIntentPhenomenon(
                                workspaceId = workspaceId,
                                expressionId = expressionId!!,
                                systemId = toolUseId
                            )

                            phenomenonId = id

                            intentCognizer = IntentCognizer(
                                workspaceId = workspaceId,
                                expressionId = expressionId,
                                phenomenonId = id,
                                repository = repository
                            )


                            emit(CognitionEvent.IntentInitiation(
                                id = id,
                                expressionId = expressionId,
                                systemId = toolUseId
                            ))
                            // we have only one tool, so we don't even check the name
                            // no emission
                        }

                    }
                }

                is Event.ContentBlockDelta -> {

                    when (event.delta) {

                        is Event.ContentBlockDelta.Delta.TextDelta -> {

                            val textDelta = (event.delta as Event.ContentBlockDelta.Delta.TextDelta).text

                            repository.appendText(
                                workspaceId = workspaceId,
                                expressionId = expressionId!!,
                                phenomenonId = phenomenonId!!,
                                textDelta = textDelta
                            )

                            emit(
                                CognitionEvent.TextUnfolding(
                                    id = phenomenonId!!,
                                    expressionId = expressionId,
                                    textDelta = textDelta
                                )
                            )

                        }

                        is Event.ContentBlockDelta.Delta.InputJsonDelta -> {
                            val jsonDelta = (event.delta as Event.ContentBlockDelta.Delta.InputJsonDelta).partialJson
                            intentCognizer!!.add(jsonDelta).forEach { result ->
                                emit(result)
                            }
                        }

                    }
                }

                is Event.ContentBlockStop -> {
                    when (processedContentType!!) {
                        ProcessedContentType.TEXT -> {
                            emit(CognitionEvent.TextCulmination(
                                id = phenomenonId!!,
                                expressionId = expressionId!!
                            ))
                        }
                        ProcessedContentType.TOOL -> {
                            emit(CognitionEvent.IntentCulmination(
                                id = phenomenonId!!,
                                expressionId = expressionId!!
                            ))
                        }
                    }
                    processedContentType = null
                    phenomenonId = null
                    intentCognizer = null
                }

                is Event.MessageStop -> {

                    val moment = repository.culminateExpression(
                        workspaceId = workspaceId,
                        expressionId = expressionId!!
                    )

                    emit(
                        CognitionEvent.ExpressionCulmination(
                            expressionId = expressionId,
                            moment = moment
                        )
                    )
                }

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
