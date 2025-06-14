/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.cognition

import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.presenter.cognition.ExpressionAppender
import com.xemantic.ai.golem.presenter.cognition.IntentAppender
import com.xemantic.ai.golem.presenter.cognition.TextAppender
import com.xemantic.ai.golem.presenter.cognition.CognitionView
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.eventFlow
import com.xemantic.ai.golem.web.js.inject
import com.xemantic.ai.golem.web.ui.Div
import com.xemantic.ai.golem.web.ui.IconButton
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.js.details
import kotlinx.html.js.div
import kotlinx.html.js.summary
import kotlinx.html.js.textArea
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent

class HtmlCognitionView(
    private val parentDiv: HTMLDivElement? = null,
) : CognitionView, HasRootHtmlElement {

    private val phenomenaDiv = Div("phenomena")

    private val micButton = IconButton(
        icon = "mic",
        ariaLabel = "Start voice input"
    )

    private val expressButton = IconButton(
        icon = "send",
        ariaLabel = "Express your intent"
    )

    private val promptInput = dom.textArea {
        placeholder = "Ask me anything..."
    }

    private val promptDiv = dom.div("prompt") {
        inject(promptInput)
        div("prompt-controls") {
            div("prompt-options") {
                inject(
                    micButton,
                    expressButton
                )
            }
            div("prompt-actions")
        }
    }

    override val element = dom.div("cognition") {
        inject(
            phenomenaDiv,
                    promptDiv
        )
    }

    override fun starExpression(
        agent: EpistemicAgent
    ): ExpressionAppender {

        val role = if (agent is EpistemicAgent.AI) {
            "assistant"
        } else {
            "user"
        }

        // TODO this should be changed a lot
        val messageDiv = dom.div("expression $role")
        phenomenaDiv.append(messageDiv)
        return object : ExpressionAppender {

            override fun textAppender(): TextAppender {
                val textDiv = dom.div("text")
                messageDiv.append(textDiv)
                return  { textDiv.append(it) }
            }

            override fun intentAppender(): IntentAppender {

                val intentDiv = dom.details("intent")
                messageDiv.append(intentDiv)

                return object : IntentAppender {

                    override fun purposeAppender(): TextAppender {
                        val purposeDiv = dom.summary("purpose")
                        intentDiv.append(purposeDiv)
                        return { purposeDiv.append(it) }
                    }

                    override fun codeAppender(): TextAppender {
                        val codeDiv = dom.div("code")
                        intentDiv.append(codeDiv)
                        return { codeDiv.append(it) }
                    }

                }

            }

        }

    }

    override fun addExpression(expression: PhenomenalExpression) {
        println("adding expression: $expression")
        phenomenaDiv.append {
            val role = if (expression.agent is EpistemicAgent.AI) {
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