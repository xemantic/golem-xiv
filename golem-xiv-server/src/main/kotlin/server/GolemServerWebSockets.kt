/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
