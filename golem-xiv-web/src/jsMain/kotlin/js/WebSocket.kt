/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.js

import kotlinx.coroutines.flow.Flow
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

//fun CoroutineScope.handleWebSocket(ws: WebSocket): Flow<GolemOutput> {
////    launch {
////        ws.openEvents.collect {
////            console.log("Connected to WebSocket")
////        }
////    }
////    launch {
////        ws.closEvents.collect {
////            console.log("WebSocket closed")
////        }
////    }
////    launch {
////        ws.errorEvents.collect {
////            console.log("WebSocket error: $it")
////        }
////    }
//    //return ws.messageEvents.toReasoningEvents()
//    return flow {
//        GolemOutput.Welcome("foo")
//    }
//}
