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

package com.xemantic.golem.web

import com.xemantic.ai.golem.api.GolemInput
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.golem.web.reasoning.DefaultReasoningView
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.rpc.internal.utils.InternalRpcApi
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.internal.KrpcPlugin
import kotlinx.serialization.json.Json
import org.w3c.dom.WebSocket

//fun main() {
//    val scope = MainScope()
//
//    val container = document.createElement("p").appendChild(document.createTextNode("dupaaaaazzzzzaaaaaaaaa"))
//    document.body!!.appendChild(container)
//}

@OptIn(InternalRpcApi::class)
suspend fun main() {
    val scope = MainScope()

    val client = HttpClient {
//        install(KrpcPlugin) {
//
//        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    client.webSocket(
        port = 8081, // TODO this port should depend on the configuration
        path = "/ws"
    ) {
        while(true) {
//            val golemOutput = receiveDeserialized<GolemOutput>()
            val othersMessage = incoming.receive() as? Frame.Text
            console.log(othersMessage!! .readText())
//            if(myMessage != null) {
//                send(myMessage)
//            }
        }
    }
    val view = DefaultReasoningView()
    document.body!!.append(view.chatDiv)

    val protocol = if (window.location.protocol == "https:") "wss" else "ws"

//    client.webSocket {  }
//    val ws = WebSocket("$protocol://localhost:8081/ws")
//    ws.onmessage = {
//        val x: String = JSON.parse(it.data as String)
//        println("Message x: $x")
//    }
//    ws.onopen = {
//        println("Open: ${JSON.stringify(it)}")
//    }
//    ws.onclose = {
//        println("Close: $it")
//    }

//    val agentOutput = scope.handleWebSocket(ws)

//    ReasoningPresenter(
//        scope,
//        view,
//        reasoning = emptyList(),
//        agentOutput,
//    ) { agentInput ->
//        //ws.send(agentInput.toJson())
//    }

}
