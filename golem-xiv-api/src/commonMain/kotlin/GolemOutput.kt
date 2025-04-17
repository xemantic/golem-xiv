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
import kotlin.uuid.Uuid

interface WithContextId {

    val contextId: Uuid

}

@Serializable
@JsonClassDiscriminator("type")
sealed class GolemOutput {

    @Serializable
    @SerialName("welcome")
    data class Welcome (
        val message: String
    ): GolemOutput()

    @Serializable
    @SerialName("message")
    data class Message (
        override val contextId: Uuid,
        val message: com.xemantic.ai.golem.api.Message
    ): GolemOutput(), WithContextId

    @Serializable
    @SerialName("textDelta")
    data class TextDelta (
        override val contextId: Uuid,
        val delta: String
    ): GolemOutput(), WithContextId

    @Serializable
    @SerialName("osProcess")
    data class OsProcess(
        override val contextId: Uuid,
        val event: OsProcessEvent
    ) : GolemOutput(), WithContextId

    @Serializable
    @SerialName("contextAdded")
    data class ContextAdded(
        override val contextId: Uuid
    ) : GolemOutput(), WithContextId

    @Serializable
    @SerialName("contextUpdated")
    data class ContextUpdated(
        override val contextId: Uuid,
        val info: ContextInfo
    ) : GolemOutput(), WithContextId

}
