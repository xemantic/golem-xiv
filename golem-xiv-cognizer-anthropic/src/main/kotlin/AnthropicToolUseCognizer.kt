/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import com.xemantic.ai.anthropic.tool.ToolChoice
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.CognitionRepository
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
    private val repository: CognitionRepository
) : Cognizer {

    private val logger = KotlinLogging.logger {}

    private val golemTools = listOf(Tool<ExecuteGolemScript>())

    override fun reason(
        constitution: List<String>,
        cognitionId: Long,
        phenomenalFlow: List<PhenomenalExpression>,
        hints: Map<String, String>
    ): Flow<CognitionEvent> {

        logger.debug {
            "Cognition[$cognitionId]: reasoning"
        }

        var expressionId: Long? = null

        var phenomenonId: Long? = null

        var intentCognizer: IntentCognizer? = null

        var textBuffer: StringBuilder? = null

        var processedContentType: ProcessedContentType? = null

        var toolUseId: String? = null

        val messageFlow = phenomenalFlow.toAnthropicMessages().addCacheBreakpoint()

        logger.trace {
            "Cognition[$cognitionId]: anthropic messages: $messageFlow"
        }

        val flow = anthropic.messages.stream {
            system = constitution.toAnthropicSystem()
            messages = messageFlow
            tools = golemTools
            toolChoice = ToolChoice.Auto {
                disableParallelToolUse = true
            }
        }.transform { event ->
            when (event) {

                is Event.MessageStart -> {

                    val agent = EpistemicAgent.AI(
                        id = golemSelfId,
                        model = event.message.model,
                        vendor = "Anthropic"
                    )

                    val info = repository.initiateExpression(cognitionId, agent)
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
                                cognitionId = cognitionId,
                                expressionId = expressionId!!,
                            )

                            phenomenonId = id
                            textBuffer = StringBuilder()

                            emit(CognitionEvent.TextInitiation(
                                id = id,
                                expressionId = expressionId
                            ))
                        }

                        is Event.ContentBlockStart.ContentBlock.ToolUse -> {

                            processedContentType = ProcessedContentType.TOOL
                            toolUseId = (event.contentBlock as Event.ContentBlockStart.ContentBlock.ToolUse).id

                            val id = repository.initiateIntentPhenomenon(
                                cognitionId = cognitionId,
                                expressionId = expressionId!!,
                                systemId = toolUseId
                            )

                            phenomenonId = id

                            intentCognizer = IntentCognizer(
                                expressionId = expressionId,
                                phenomenonId = id
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

                            textBuffer!!.append(textDelta)

                            emit(
                                CognitionEvent.TextUnfolding(
                                    id = phenomenonId!!,
                                    expressionId = expressionId!!,
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
                            repository.culminateTextPhenomenon(
                                cognitionId = cognitionId,
                                expressionId = expressionId!!,
                                phenomenonId = phenomenonId!!,
                                text = textBuffer.toString()
                            )
                            emit(CognitionEvent.TextCulmination(
                                id = phenomenonId!!,
                                expressionId = expressionId
                            ))
                            textBuffer = null
                        }
                        ProcessedContentType.TOOL -> {
                            repository.culminateIntentPhenomenon(
                                cognitionId = cognitionId,
                                expressionId = expressionId!!,
                                phenomenonId = phenomenonId!!,
                                purpose = intentCognizer!!.purpose,
                                code = intentCognizer!!.code
                            )
                            emit(CognitionEvent.IntentCulmination(
                                id = phenomenonId!!,
                                expressionId = expressionId
                            ))
                        }
                    }
                    processedContentType = null
                    phenomenonId = null
                    intentCognizer = null
                }

                is Event.MessageStop -> {

                    val moment = repository.culminateExpression(
                        cognitionId = cognitionId,
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
            logger.debug { "Cognition[$cognitionId]: API streaming start" }
        }.onCompletion {
            logger.debug { "Cognition[$cognitionId]: API streaming stop" }
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
