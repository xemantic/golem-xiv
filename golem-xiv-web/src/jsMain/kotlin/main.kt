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

import com.xemantic.golem.web.js.handleWebSocket
import com.xemantic.golem.web.reasoning.DefaultReasoningView
import com.xemantic.golem.web.reasoning.ReasoningPresenter
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import org.w3c.dom.WebSocket

//fun main() {
//    val scope = MainScope()
//
//    val container = document.createElement("p").appendChild(document.createTextNode("dupaaaaazzzzzaaaaaaaaa"))
//    document.body!!.appendChild(container)
//}

fun main() {

    val scope = MainScope()

    val view = DefaultReasoningView()
    document.body!!.append(view.chatDiv)

    val ws = WebSocket("ws://localhost:8081/ws")

    val agentOutput = scope.handleWebSocket(ws)

    ReasoningPresenter(
        scope,
        view,
        reasoning = emptyList(),
        agentOutput,
    ) { agentInput ->
        //ws.send(agentInput.toJson())
    }

}
