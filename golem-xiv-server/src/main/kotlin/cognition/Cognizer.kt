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

package com.xemantic.ai.golem.server.cognition

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.golem.api.Message
import com.xemantic.ai.golem.api.ReasoningEvent
import com.xemantic.ai.golem.server.cognition.anthropic.AnthropicCognizer
import kotlinx.coroutines.flow.Flow

interface Cognizer {

    fun reason(
        system: List<String>,
        conversation: List<Message>,
        hints: Map<String, String>
    ): Flow<ReasoningEvent>

}

private val defaultCognizer = AnthropicCognizer(
    Anthropic()
)

//private val defaultCognizer = DashscopeCognizer(
//    Generation(Protocol.HTTP.value, "https://dashscope-intl.aliyuncs.com/api/v1")
//)

fun cognizer(): Cognizer = defaultCognizer
