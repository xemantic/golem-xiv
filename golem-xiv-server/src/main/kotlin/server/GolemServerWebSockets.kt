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

package com.xemantic.ai.golem.server.server

import com.xemantic.ai.golem.api.GolemInput
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.collectGolemData
import com.xemantic.ai.golem.api.sendGolemData
import io.ktor.websocket.WebSocketSession

suspend fun WebSocketSession.sendGolemOutput(
    output: GolemOutput
) {
    sendGolemData<GolemOutput>(output)
}

suspend fun WebSocketSession.collectGolemInput(
    block: suspend (GolemInput) -> Unit
) {
    collectGolemData<GolemInput>(block)
}
