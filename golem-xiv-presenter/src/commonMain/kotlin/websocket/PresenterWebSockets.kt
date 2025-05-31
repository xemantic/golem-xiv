/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.websocket

import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.client.websocket.collectGolemData
import io.ktor.websocket.WebSocketSession

//suspend fun WebSocketSession.sendToGolem(
//    input: GolemInput
//) {
//    val json = golemJson.encodeToString<GolemInput>(input)
//    send(json)
//}

suspend fun WebSocketSession.collectGolemOutput(
    block: suspend (GolemOutput) -> Unit
) {
    collectGolemData<GolemOutput>(block)
}
