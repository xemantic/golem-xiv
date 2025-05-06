/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.websocket

import com.xemantic.ai.golem.api.GolemInput
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.collectGolemData
import com.xemantic.ai.golem.api.golemJson
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send

suspend fun WebSocketSession.sendToGolem(
    input: GolemInput
) {
    val json = golemJson.encodeToString<GolemInput>(input)
    send(json)
}

suspend fun WebSocketSession.collectGolemOutput(
    block: suspend (GolemOutput) -> Unit
) {
    collectGolemData<GolemOutput>(block)
}
