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

package com.xemantic.ai.golem.web.context

import com.xemantic.ai.golem.api.Message
import com.xemantic.ai.golem.presenter.context.ContextView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.injector.inject
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.view.HtmlView
import com.xemantic.ai.golem.web.js.eventFlow
import com.xemantic.ai.golem.web.js.icon
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.js.button
import kotlinx.html.js.div
import kotlinx.html.js.textArea
import kotlinx.html.span
import org.w3c.dom.events.InputEvent

class HtmlContextView : ContextView, HtmlView {

    private val content = document.create.div("content")

    private val messagesDiv = document.create.div("messages")

    private val promptInput = document.create.textArea {
        id = "prompt-input"
        placeholder = "Ask me anything..."
    }

    private val micButton = document.create.button {
        id = "mic-button"
        ariaLabel = "Start voice input"
        icon("microphone")
    }

    private val sendButton = document.create.button {
        id = "send-button"
        ariaLabel = "Send message"
        icon("paper-plane")
    }

    private val micStatus = document.create.div(
        "hidden"
    ) {
        id = "mic-status"
        +"Listening... "
        span {
            id = "recording-time"
            +"0:00"
        }
        button {
            id = "stop-recording"
            icon("stop")
        }
    }

    private val submitButton = document.create.button { +"Send" }

    override val element = document.create.inject(
        promptInput to "#prompt-box",
        //toggleThemeButton to ".sidebar-footer"
    ).div("chat-centered-mode") {
        id = "chat-container"
        div {
            id = "messages"
            div { id = "input-container"
                div { id = "prompt-box" }
            }
            h2("Conversation")
            button(classes = "new-chat-btn") {
                icon("plus"); +" New Chat"
            }
        }
//        div("sidebar-content") // TODO
//        div("sidebar-footer")
    }

    override fun addMessage(message: Message) {
        messagesDiv.append {
            div("message") {

            }

            message.content.forEach {

            }
        }

        TODO("Not yet implemented")
    }

    override val promptChanges: Flow<String> =
        promptInput.eventFlow<InputEvent>("input").map {
            promptInput.value
        }

    override val promptSubmits: Flow<Action> = submitButton.actions()

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



