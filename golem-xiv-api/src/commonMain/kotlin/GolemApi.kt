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

package com.xemantic.ai.golem.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.time.Instant
import kotlin.uuid.Uuid

// things which go over web socket

@Serializable
sealed interface Content

@Serializable
data class ContextInfo(
    val id: Uuid,
    val title: String,
    // TODO move it to general file level
    @Serializable(with = InstantIso8601Serializer::class)
    val creationDate: Instant
) : Content

@Serializable
data class Message(
    val id: Uuid,
    val contextId: Uuid,
    val role: Role = Role.USER,
    val content: List<Content>
) {

    enum class Role {
        USER,
        ASSISTANT
    }

}

@Serializable
@SerialName("text")
data class Text(
    val text: String
) : Content

@Serializable
@SerialName("image")
data class Image(
    val path: String
) : Content

@Serializable
@SerialName("document")
data class Document(
    val path: String
) : Content

/** Added to user messages to indicate called services/tools, not sent to LLM */
@Serializable
@SerialName("call")
data class Call(
    val name: String,
    val description: String
) : Content

@Serializable
class ContentDelta(
    val conversationId: Uuid,
    val delta: Text
)

@Serializable
data class Prompt(
    val content: List<Content>
)

@Serializable
@JsonClassDiscriminator("type")
sealed interface ReasoningEvent {

    @Serializable
    @SerialName("messageStart")
    data class MessageStart(
        val messageId: Uuid,
        val role: Message.Role
    ) : ReasoningEvent

    @Serializable
    @SerialName("messageStop")
    data class MessageStop(
        val messageId: Uuid,
    ) : ReasoningEvent

    @Serializable
    @SerialName("textContentStart")
    data class TextContentStart(
        val messageId: Uuid
    ) : ReasoningEvent

    @Serializable
    @SerialName("textContentDelta")
    data class TextContentDelta(
        val messageId: Uuid,
        val delta: String
    ) : ReasoningEvent

    @Serializable
    @SerialName("textContentStop")
    data class TextContentStop(
        val messageId: Uuid
    ) : ReasoningEvent

    @Serializable
    @SerialName("scriptRun")
    data class ScriptRun(
        val id: Uuid
    ) : ReasoningEvent

    @Serializable
    @SerialName("scriptFinished")
    data class ScriptFinished(
        val id: Uuid
    ) : ReasoningEvent



    @Serializable
    @SerialName("contextTitle")
    data class ContextTitle(
        val title: String
    ) : ReasoningEvent

//    @Serializable
//    @SerialName("recursiveCognition")
//    data class RecursiveCognition(
//        val id: Uuid
//    )

}

//@Serializable
//sealed class Content {
//
//    abstract val conversationId: String
//
//    @Serializable
//    data class ServiceExecution(
//        override val conversationId: String,
//        val service: String,
//        val purpose: String
//    ) : Content()
//
//    @Serializable
//    data class Text(
//        override val conversationId: String,
//        val service: String,
//    ) : Content()
//
//}
