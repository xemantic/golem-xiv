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
import com.xemantic.ai.anthropic.message.Role
import com.xemantic.ai.anthropic.message.System
import com.xemantic.ai.golem.api.Content
import com.xemantic.ai.golem.api.Message
import com.xemantic.ai.golem.api.ReasoningEvent
import com.xemantic.ai.golem.api.Text
import com.xemantic.ai.golem.server.cognition.Cognizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal fun Content.toAnthropic() = when (this) {
    is Text -> toAnthropic()
    else -> throw IllegalStateException("Unsupported content type")
}

internal fun List<Content>.toAnthropic2() = map { it.toAnthropic() }

internal fun Message.toAnthropic() = com.xemantic.ai.anthropic.message.Message {
    role = this@toAnthropic.role.toAnthropic()
    content = this@toAnthropic.content.toAnthropic2()
}

internal fun Message.Role.toAnthropic() = when (this) {
    Message.Role.USER -> Role.USER
    Message.Role.ASSISTANT -> Role.ASSISTANT
}

internal fun List<Message>.toAnthropic() = map {
    it.toAnthropic()
}

fun Text.toAnthropic() = com.xemantic.ai.anthropic.content.Text(text)

fun List<String>.toAnthropicSystem() = map {
    System(text = it)
}

class AnthropicCognizer(
    private val anthropic: Anthropic
) : Cognizer {

    override fun reason(
        system: List<String>,
        conversation: List<Message>,
        hints: Map<String, String>
    ): Flow<ReasoningEvent> = flow {
        val response = anthropic.messages.create {
            this.system = system.toAnthropicSystem()
            messages = conversation.toAnthropic()
        }
        emit(ReasoningEvent.MessageStart(role = Message.Role.ASSISTANT))
        emit(ReasoningEvent.TextContentStart())
        emit(ReasoningEvent.TextContentDelta(response.text!!))
        emit(ReasoningEvent.TextContentEnd())
        emit(ReasoningEvent.MessageEnd())
    }

}
