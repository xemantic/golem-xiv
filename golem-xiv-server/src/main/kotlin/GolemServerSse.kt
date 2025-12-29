/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server

import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.golemJson
import io.ktor.server.sse.ServerSSESession
import io.ktor.sse.ServerSentEvent

suspend fun ServerSSESession.sendGolemOutput(
    output: GolemOutput
) {
    val data = golemJson.encodeToString<GolemOutput>(output)
    send(ServerSentEvent(data = data))
}
