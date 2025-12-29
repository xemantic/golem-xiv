/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.sse

import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.golemJson
import io.ktor.client.plugins.sse.SSESession
import kotlinx.coroutines.flow.mapNotNull

suspend fun SSESession.collectGolemOutput(
    block: suspend (GolemOutput) -> Unit
) {
    incoming.mapNotNull { event ->
        event.data?.let { golemJson.decodeFromString<GolemOutput>(it) }
    }.collect(block)
}
