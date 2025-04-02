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

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

// things which go over web socket

interface Content

@Serializable
data class Context(
    val id: Uuid,
    val system: List<Content>,
    val messages: List<Message>
) : Content {

    @Serializable
    data class Info @OptIn(ExperimentalTime::class) constructor(
        val id: Uuid,
        val title: String,
        // TODO move it to general file level
        @Serializable(with = InstantIso8601Serializer::class)
        val creationDate: Instant
    )

}

@Serializable
data class Message(
    val role: Role = Role.USER,
    val content: List<Content>
) {

    enum class Role {
        USER,
        ASSISTANT
    }

}

@Serializable
data class Text(
    val text: String
) : Content

@Serializable
data class Image(
    val path: String
) : Content

data class Document(
    val path: String
)

data class Code(
    val kotlinScript: String
)

@Serializable
class ContentDelta(
    val conversationId: Uuid,
    val delta: Text
)

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
