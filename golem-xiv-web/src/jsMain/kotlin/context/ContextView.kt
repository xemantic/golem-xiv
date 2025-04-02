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

package com.xemantic.golem.web.context

import com.xemantic.golem.web.js.eventFlow
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.button
import kotlinx.html.js.div
import kotlinx.html.js.textArea
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.MouseEvent

class DefaultContextView : ContextView {

    private val content = document.create.div("content")

    private val promptInput = document.create.textArea {}

    private val submitButton = document.create.button { +"Send" }

    val chatDiv = document.create.div("chat").apply {
        appendChild(content)
        appendChild(promptInput)
        appendChild(submitButton)
    }

    override val promptChanges: Flow<String> =
        promptInput.eventFlow<InputEvent>("input").map {
            promptInput.value
        }

    override val promptSubmits: Flow<Aciton> =
        submitButton.eventFlow<MouseEvent>("click").map {
            Aciton
        }

    override var promptInputDisabled: Boolean
        get() = promptInput.disabled
        set(value) {
            promptInput.disabled = value
        }

    override fun clearPromptInput() {
        promptInput.value = ""
    }

    override var promptSubmitDisabled: Boolean
        get() = submitButton.disabled
        set(value) {
            submitButton.disabled = value
        }

    override fun addWelcomeMessage(text: String) {
        content.append {
            div("welcome") {
                +text
            }
        }
    }

    override fun addTextResponse(text: String) {
        content.append {
            div("text") {
                +text
            }
        }
    }

//    override fun addToolUseRequest(request: AgentOutput.ToolUseRequest) {
//        content.append.div("tool-use") {
//            div("tool") {
//                +request.tool::class.simpleName!!
//            }
//            div("purpose") {
//                +request.tool.purpose
//            }
//            when (request.tool) {
//                is ExecuteShellCommand -> {
//                    div("command") {
//                        request.tool.command
//                    }
//                }
//                else -> {}
//            }
//
//        }
//    }
//
//    override fun addToolUseResponse(
//        response: AgentOutput.ToolUseResponse
//    ) {
//        content.append.div("tool-use-response") {
//            response.toolResult.content!!.forEach { content ->
//                if (content is Text) {
//                    div("text") {
//                        +content.text
//                    }
//                }
//            }
//        }
//    }

}