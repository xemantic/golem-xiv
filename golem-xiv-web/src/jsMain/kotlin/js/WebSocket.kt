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

package com.xemantic.golem.web.js

import com.xemantic.ai.golem.api.GolemOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket

val WebSocket.openEvents: Flow<MessageEvent> get() = eventFlow("open")

val WebSocket.messageEvents: Flow<MessageEvent> get() = eventFlow("message")

val WebSocket.closEvents: Flow<MessageEvent> get() = eventFlow("close")

val WebSocket.errorEvents: Flow<MessageEvent> get() = eventFlow("error")

fun Flow<MessageEvent>.toReasoningEvents() = transform { event ->
    console.log("WebSocket message received, type: ${event.type}, , type: ${event.data}")
    if (event.type == "message") {
        //val agentOutput = AgentOutput.fromJson(event.data as String)
        val output = "foo"
        // TODO fix it
        //emit(GolemOutput.Welcome(output))
        emit("hi")
    } else {
        console.error("Unsupported event type: ${event.type}")
    }
}

fun CoroutineScope.handleWebSocket(ws: WebSocket): Flow<GolemOutput> {
//    launch {
//        ws.openEvents.collect {
//            console.log("Connected to WebSocket")
//        }
//    }
//    launch {
//        ws.closEvents.collect {
//            console.log("WebSocket closed")
//        }
//    }
//    launch {
//        ws.errorEvents.collect {
//            console.log("WebSocket error: $it")
//        }
//    }
    //return ws.messageEvents.toReasoningEvents()
    return flow {
        GolemOutput.Welcome("foo")
    }
}
