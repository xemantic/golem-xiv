/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.phenomena

import com.xemantic.ai.golem.api.Agent
import com.xemantic.ai.golem.api.Expression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.presenter.phenomena.ExpressionAppender
import com.xemantic.ai.golem.presenter.phenomena.WorkspaceView
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.view.HtmlView
import com.xemantic.ai.golem.web.js.eventFlow
import com.xemantic.ai.golem.web.js.icon
import com.xemantic.ai.golem.web.util.appendTo
import com.xemantic.ai.golem.web.util.children
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.dom.appendText
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.button
import kotlinx.html.js.div
import kotlinx.html.js.textArea
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent

class HtmlWorkspaceView : WorkspaceView, HtmlView {

    private val messagesDiv = html.div("messages")

    private val micButton = html.button {
        id = "mic-button"
        ariaLabel = "Start voice input"
        icon("microphone")
    }

    private val sendButton = html.button {
        id = "send-button"
        ariaLabel = "Send message"
        icon("paper-plane")
    }

    private val promptInput = html.textArea {
        placeholder = "Ask me anything..."
    }

    private val promptDiv = html.div("prompt").children(
        promptInput,
        html.div("prompt-controls") {
            div("prompt-options")
            div("prompt-actions")
        }.appendTo(
            ".prompt-actions",
            micButton,
            sendButton
        )

//                children(
//            micButton,
//            sendButton
//        )
    )

    override val element = html.div("workspace").children(
        messagesDiv,
        promptDiv
    )

    override fun starExpression(agent: Agent): ExpressionAppender {
        val role = if (agent.category == Agent.Category.SELF) {
            "assistant"
        } else {
            "user"
        }
        // TODO this should be changed a lot
        val messageDiv = html.div("message $role")
        messagesDiv.append(messageDiv)
        return object : ExpressionAppender {
            override fun append(text: String) {
                messageDiv.appendText(text)
            }
            override fun finalize() {
                // nothing to do
            }
        }
    }

    override fun addExpression(expression: Expression) {
        println("adding expression: $expression")
        messagesDiv.append {
            val role = if (expression.agent.category == Agent.Category.SELF) {
                "assistant"
            } else {
                "user"
            }
            // TODO this should be changed a lot
            div("message $role") {
                expression.phenomena.forEach {
                    div("content") {
                        when (it) {
                            is Phenomenon.Text -> { +it.text }
                            else -> { +it.toString() }
                        }
                    }
                }
            }
        }
    }

    override val promptChanges = promptInput.eventFlow<InputEvent>("input").map {
        promptInput.value
    }

    override val promptInputShiftKeys = merge(
        promptInput.eventFlow<KeyboardEvent>("keydown").map { it.shiftKey },
        promptInput.eventFlow<KeyboardEvent>("keyup").map { false }
    )

    override fun updatePromptInputHeight() {
        promptInput.adjustHeight()
    }

    override val sendActions = sendButton.actions()

    override var promptInputDisabled: Boolean
        get() = promptInput.disabled
        set(value) {
            promptInput.disabled = value
        }

    override fun clearPromptInput() {
        promptInput.value = ""
    }

    override var sendDisabled: Boolean
        get() = sendButton.disabled
        set(value) {
            sendButton.disabled = value
        }

//    override fun addTextResponse(text: String) {
////        content.append {
////            div("text") {
////                +text
////            }
////        }
//    }

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

    private fun HTMLTextAreaElement.adjustHeight() {
        style.height = "auto"
        style.height = "${scrollHeight}px"
    }

}
