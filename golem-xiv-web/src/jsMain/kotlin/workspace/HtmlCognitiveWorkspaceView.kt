/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.workspace

import com.xemantic.ai.golem.api.Agent
import com.xemantic.ai.golem.api.Expression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.presenter.phenomena.ExpressionAppender
import com.xemantic.ai.golem.presenter.phenomena.IntentAppender
import com.xemantic.ai.golem.presenter.phenomena.TextAppender
import com.xemantic.ai.golem.presenter.phenomena.CognitiveWorkspaceView
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.ui.div
import com.xemantic.ai.golem.web.js.eventFlow
import com.xemantic.ai.golem.web.ui.iconButton
import com.xemantic.ai.golem.web.util.appendTo
import com.xemantic.ai.golem.web.util.children
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.document
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.textArea
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent

class HtmlCognitiveWorkspaceView : CognitiveWorkspaceView, HasRootHtmlElement {

    private val phenomenaDiv = div("phenomena")

    private val micButton = iconButton(
        icon = "mic",
        ariaLabel = "Start voice input"
    )

    private val expressButton = iconButton(
        icon = "send",
        ariaLabel = "Express your intent"
    )

    private val promptInput = document.create.textArea {
        placeholder = "Ask me anything..."
    }

    private val promptDiv = div("prompt").children(
        promptInput,
        document.create.div("prompt-controls") {
            div("prompt-options")
            div("prompt-actions")
        }.appendTo(
            ".prompt-actions",
            micButton,
            expressButton
        )

//                children(
//            micButton,
//            sendButton
//        )
    )

    override val element = div("cognitive-workspace").children(
        phenomenaDiv,
        promptDiv
    )

    override fun starExpression(agent: Agent): ExpressionAppender {

        val role = if (agent.category == Agent.Category.SELF) {
            "assistant"
        } else {
            "user"
        }

        // TODO this should be changed a lot
        val messageDiv = div("expression $role")
        phenomenaDiv.append(messageDiv)
        return object : ExpressionAppender {

            override fun textAppender(): TextAppender {
                val textDiv = div("text")
                messageDiv.append(textDiv)
                return  { textDiv.append(it) }
            }

            override fun intentAppender(): IntentAppender {

                val intentDiv = div("intent")
                messageDiv.append(intentDiv)

                return object : IntentAppender {

                    override fun purposeAppender(): TextAppender {
                        val purposeDiv = div("purpose")
                        intentDiv.append(purposeDiv)
                        return { purposeDiv.append(it) }
                    }

                    override fun codeAppender(): TextAppender {
                        val codeDiv = div("code")
                        intentDiv.append(codeDiv)
                        return { codeDiv.append(it) }
                    }

                }

            }

        }

    }

    override fun addExpression(expression: Expression) {
        println("adding expression: $expression")
        phenomenaDiv.append {
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

    override val sendActions = expressButton.actions()

    override var promptInputDisabled: Boolean
        get() = promptInput.disabled
        set(value) {
            promptInput.disabled = value
        }

    override fun clearPromptInput() {
        promptInput.value = ""
    }

    override var sendDisabled: Boolean
        get() = expressButton.disabled
        set(value) {
            expressButton.disabled = value
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