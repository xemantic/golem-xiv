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

package com.xemantic.ai.golem.server

import com.xemantic.ai.golem.api.GolemOutput
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.origin
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import java.io.File

private val logger = KotlinLogging.logger {}

fun main() {
    val golem = Golem()
    startServer(golem)
}

fun startServer(
    golem: Golem
) {
    embeddedServer(CIO, 8081) {
        install(WebSockets) {
        }
        routing {
            staticFiles(dir = File("src/web"), remotePath = "/")
            webSocket("/ws") {
                val clientIp = call.request.origin.remoteAddress
                logger.info { "web socket connected: $clientIp" }
                incoming.consumeEach {
                    sendSerialized(GolemOutput.Welcome("You are connected to Golem XIV"))
                }
                try {
                    val context = golem.newContext()

//                while (true) {
//                    val frame = incoming.receive()
//                    if (frame !is Frame.Text) {              logger.error { "Received frame is not a text: ${frame.frameType}" }
//                        send(AgentOutput.Error("Received frame is not a text: ${frame.frameType}"))              continue
//                    }            val receivedText = frame.readText()
//                    val message = try {
//                        AgentInput.fromJson(receivedText)            } catch (e: SerializationException) {
//                        logger.error { "Unrecognized JSON message: $receivedText" }              send(AgentOutput.Error("Unrecognized JSON message: ${frame.frameType}"))
//                        continue            }
//                    claudineSession.process(message) {              send(it)
//                    }
//                }        } catch (e: Exception) {

                } catch (e: Exception) {
                    logger.error {
                        "Unexpected error in WebSocket session: ${e.message}"
                    }
                }
            }
        }
    }.start(wait = true)

}
