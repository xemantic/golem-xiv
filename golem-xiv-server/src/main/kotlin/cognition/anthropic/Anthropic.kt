/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.ai.golem.server.cognition.anthropic

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.cache.CacheControl
import com.xemantic.ai.anthropic.collections.transformLast
import com.xemantic.ai.anthropic.event.Delta
import com.xemantic.ai.anthropic.event.Event
import com.xemantic.ai.anthropic.message.Role
import com.xemantic.ai.anthropic.message.System
import com.xemantic.ai.golem.api.Content
import com.xemantic.ai.golem.api.Message
import com.xemantic.ai.golem.api.ReasoningEvent
import com.xemantic.ai.golem.api.Text
import com.xemantic.ai.golem.server.cognition.Cognizer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlin.uuid.Uuid

internal fun Content.toAnthropicContent() = when (this) {
    is Text -> toAnthropicText()
    else -> throw IllegalStateException("Unsupported content type")
}

internal fun List<Content>.toAnthropicContent() = map { it.toAnthropicContent() }

internal fun Message.toAnthropicMessage() = com.xemantic.ai.anthropic.message.Message {
    role = this@toAnthropicMessage.role.toAnthropic()
    content = this@toAnthropicMessage.content.toAnthropicContent()
}

internal fun Message.Role.toAnthropic() = when (this) {
    Message.Role.USER -> Role.USER
    Message.Role.ASSISTANT -> Role.ASSISTANT
}

internal fun List<Message>.toAnthropicMessages() = map {
    it.toAnthropicMessage()
}

fun Text.toAnthropicText() = com.xemantic.ai.anthropic.content.Text(text)

fun List<String>.toAnthropicSystem() = map {
    System(text = it, cacheControl = CacheControl.Ephemeral())
}

class AnthropicCognizer(
    private val anthropic: Anthropic
) : Cognizer {

    private val logger = KotlinLogging.logger {}

    override fun reason(
        system: List<String>,
        conversation: List<Message>,
        hints: Map<String, String>
    ): Flow<ReasoningEvent> {

        logger.debug { "Anthropic API: Streaming start" }

        val messageId = Uuid.random()

        val flow = anthropic.messages.stream {
            this.system = system.toAnthropicSystem()
            messages = conversation.toAnthropicMessages().transformLast {
                copy {
                    content = content.transformLast {
                        alterCacheControl(
                            CacheControl.Ephemeral()
                        )
                    }
                }
            }
        }.transform { event ->
            when (event) {
                is Event.MessageStart -> ReasoningEvent.MessageStart(
                    messageId,
                    role = Message.Role.ASSISTANT
                )
                is Event.ContentBlockStart -> ReasoningEvent.TextContentStart(messageId)
                is Event.ContentBlockDelta -> ReasoningEvent.TextContentDelta(messageId, (event.delta as Delta.TextDelta).text)
                is Event.ContentBlockStop -> ReasoningEvent.TextContentStop(messageId)
                is Event.MessageStop -> ReasoningEvent.MessageStop(messageId)
                else -> null
            }?.let {
                emit(it)
            }
        }

        logger.debug { "Anthropic API: Streaming finished" }

        return flow
    }

}
