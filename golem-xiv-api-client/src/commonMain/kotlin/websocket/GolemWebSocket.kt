/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.client.websocket

import com.xemantic.ai.golem.api.client.golemJson
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach

@PublishedApi
internal val logger = KotlinLogging.logger {}

suspend inline fun <reified T> WebSocketSession.sendGolemData(
    data: T
) {
    send(encodeToFrame<T>(data))
}

inline fun <reified T> encodeToFrame(data: T): Frame.Text {
    val json = golemJson.encodeToString<T>(data)
    return Frame.Text(json)
}

inline fun <reified T> Frame.Text.decodeFromFrame(): T {
    val data = golemJson.decodeFromString<T>(readText())
    return data
}

suspend inline fun <reified T> WebSocketSession.collectGolemData(
    crossinline block: suspend (T) -> Unit
) = incoming.consumeAsFlow().collectGolemData<T>(block)

suspend inline fun <reified T> Flow<Frame>.collectGolemData(
    crossinline block: suspend (T) -> Unit
) {
    onEach {
        if (it !is Frame.Text) {
            logger.error {
                "Unsupported frame $it"
            }
        }
    }
        .filterIsInstance<Frame.Text>()
        .collect {
            val data = it.decodeFromFrame<T>()
            block(data)
        }
}
