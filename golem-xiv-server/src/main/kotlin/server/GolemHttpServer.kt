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

import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.server.Golem
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.CompressedFileType
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.origin
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket

private val logger = KotlinLogging.logger {}

fun Application.installGolemHttp(golem: Golem) {
    install(CallLogging) {
        level = org.slf4j.event.Level.DEBUG
    }
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        // Allow requests from any host
        anyHost()

        // Or specify allowed hosts
        // allowHost("example.com")
        // allowHost("localhost:3000")

        // Configure allowed HTTP methods
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        // Allow headers
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        // Allow credentials (cookies, etc.)
        allowCredentials = true

        // Allow specific content types
        allowHeadersPrefixed("X-Custom-")

        // Configure max age for preflight requests cache
        maxAgeInSeconds = 3600
    }
    install(WebSockets) {
//        contentConverter = KotlinxWebsocketSerializationConverter(Json)
//        pingPeriod = 15.seconds
//        timeout = 15.seconds
//        maxFrameSize = Long.MAX_VALUE
//        masking = false
    }
    routing {

        staticResources("/", "web") {
            preCompressed(CompressedFileType.BROTLI, CompressedFileType.GZIP)
        }

        route("/api") {
            golemApiRoute(golem)
        }

        webSocket("/ws") {

            val clientIp = call.request.origin.remoteAddress
            logger.info { "WebSocket connected: $clientIp" }

            sendGolemOutput(
                GolemOutput.Welcome("You are connected to Golem XIV")
            )

//            val frame = incoming.receive()
//            logger.info { frame }
//            val input = receiveGolemInput()
//            println(input)
//

            collectGolemInput {
                logger.debug { it }
            }

//                try {
//                    val context = golem.newContext()
//
////                while (true) {
////                    val frame = incoming.receive()
////                    if (frame !is Frame.Text) {              logger.error { "Received frame is not a text: ${frame.frameType}" }
////                        send(AgentOutput.Error("Received frame is not a text: ${frame.frameType}"))              continue
////                    }            val receivedText = frame.readText()
////                    val message = try {
////                        AgentInput.fromJson(receivedText)            } catch (e: SerializationException) {
////                        logger.error { "Unrecognized JSON message: $receivedText" }              send(AgentOutput.Error("Unrecognized JSON message: ${frame.frameType}"))
////                        continue            }
////                    claudineSession.process(message) {              send(it)
////                    }
////                }        } catch (e: Exception) {
//
//                } catch (e: Exception) {
//                    logger.error {
//                        "Unexpected error in WebSocket session: ${e.message}"
//                    }
//                }
        }
    }
}


