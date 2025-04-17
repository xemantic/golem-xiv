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

import com.xemantic.ai.golem.api.service.GolemServiceException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.time.Instant

@PublishedApi
internal val logger = KotlinLogging.logger {}

val golemJson = Json {
    ignoreUnknownKeys = false
}

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

object InstantIso8601Serializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("kotlin.time.Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant =
        Instant.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

}

suspend inline fun <reified T> HttpClient.serviceGet(
    uri: String
): T = get(uri).run {
    if (status.isSuccess()) {
        body<T>()
    } else {
        throw GolemServiceException(
            uri,
            "${status.value} (${status.description})"
        )
    }
}

suspend inline fun <reified I, reified O> HttpClient.servicePut(
    uri: String,
    value: I
): O = put(uri) {
    setBody<I>(value)
    contentType(ContentType.Application.Json)
}.run {
    if (status.isSuccess()) {
        body<O>()
    } else {
        throw GolemServiceException(
            uri,
            "${status.value} (${status.description})"
        )
    }
}
