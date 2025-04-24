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
    val id: Uuid = Uuid.random(),
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

@Serializable
@SerialName("code")
data class Code(
    val kotlinScript: String
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
    data class MessageStart(val role: Message.Role) : ReasoningEvent

    @Serializable
    @SerialName("textContentStart")
    class TextContentStart : ReasoningEvent {
        override fun toString(): String = "TextContentStart"
    }

    @Serializable
    @SerialName("textContentDelta")
    data class TextContentDelta(val delta: String) : ReasoningEvent

    @Serializable
    @SerialName("textContentStop")
    class TextContentStop : ReasoningEvent {
        override fun toString(): String = "TextContentStop"
    }

    @Serializable
    @SerialName("scriptStart")
    data class ScriptStart(
        val purpose: String
    ) : ReasoningEvent

    @Serializable
    @SerialName("scriptDelta")
    data class ScriptDelta(val delta: String) : ReasoningEvent

    @Serializable
    @SerialName("ScriptStop")
    class ScriptStop : ReasoningEvent {
        override fun toString(): String = "ScriptStop"
    }

    @Serializable
    @SerialName("messageStop")
    class MessageStop : ReasoningEvent {
        override fun toString(): String = "MessageStop"
    }

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
