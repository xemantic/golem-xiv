/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.golem.web.cognition

import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.presenter.cognition.*
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.eventFlow
import com.xemantic.ai.golem.web.ui.Div
import com.xemantic.ai.golem.web.ui.IconButton
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import com.xemantic.kotlin.js.dom.html.*
import com.xemantic.kotlin.js.dom.node
import kotlinx.browser.window
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
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

    private val promptInput = node {
        textarea(placeholder = "Another question from the carbon side...")
    }

    private val promptDiv = node {
        div("prompt surface-container round") {
            +promptInput
            div("prompt-controls") {
                div("prompt-options") {
                    +micButton
                }
                div("prompt-actions") {
                    +expressButton
                }
            }
        }
    }

    override val element = node {
        div("cognition") {
            +phenomenaDiv
            +promptDiv
        }
    }

    override fun starExpression(
        agent: EpistemicAgent
    ): ExpressionAppender {

        val messageDiv = node {
            div("expression surface-container round ${agent.cssClass()}") {
                div("expression-header") {
                    when (agent) {
                        is EpistemicAgent.Human -> { icon("person"); +"You" }
                        is EpistemicAgent.AI -> { icon("smart_toy"); +"Golem XIV" }
                        is EpistemicAgent.Computer -> { icon("computer"); +"Computer" }
                    }
                }
            }
        }

        phenomenaDiv.append(messageDiv)

        return object : ExpressionAppender {

            override fun textAppender(): TextAppender {
                val textDiv = Div("text")
                messageDiv.append(textDiv)
                return  { textDiv.append(it) }
            }

            override fun intentAppender(): IntentAppender {

                val intentDiv = node { details("intent") }
                messageDiv.append(intentDiv)

                return object : IntentAppender {

                    override fun purposeAppender(): TextAppender {
                        val purposeTextDiv = node { div("max") }
                        val purposeDiv = node {
                            summary {
                                article("round border no-elevate") {
                                    nav {
                                        +purposeTextDiv
                                        icon("keyboard_arrow_down")
                                    }
                                }
                            }
                        }
                        intentDiv.append(purposeDiv)
                        return { purposeTextDiv.append(it) }
                    }

                    override fun codeAppender(): TextAppender {
                        val codeDiv = node { div("code round") }
                        intentDiv.append(codeDiv)
                        return { codeDiv.append(it) }
                    }

                }

            }

            override fun fulfillmentAppender(): FulfillmentAppender {

                val fulfillmentDiv = node { details("fulfillment") }
                messageDiv.append(fulfillmentDiv)

                val summaryDiv = node {
                    summary {
                        article("round border no-elevate") {
                            nav {
                                div("max") { +"Answer" }
                                icon("keyboard_arrow_down")
                            }
                        }
                    }
                }
                fulfillmentDiv.append(summaryDiv)

                return object : FulfillmentAppender {

                    override fun textAppender(): TextAppender {
                        val textDiv = node { div("text") }
                        fulfillmentDiv.append(textDiv)
                        return { textDiv.append(it) }
                    }

                }

            }

        }

    }

    override fun addExpression(expression: PhenomenalExpression) {
        println("adding expression: $expression")
        val role = expression.agent.cssClass()
        val expressionDiv = node {
            div("expression round $role") {
                expression.phenomena.forEach { phenomenon ->
                    div("content") {
                        when (phenomenon) {
                            is Phenomenon.Text -> { +phenomenon.text }
                            else -> { +phenomenon.toString() }
                        }
                    }
                }
            }
        }
        phenomenaDiv.append(expressionDiv)
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

    override var cognizing: Boolean = false
        set(value) {
            field = value
            window.asDynamic().cognizing = value
        }

    private fun HTMLTextAreaElement.adjustHeight() {
        style.height = "auto"
        style.height = "${scrollHeight}px"
    }

    private fun EpistemicAgent.cssClass() = when (this) {
        is EpistemicAgent.Human -> "human"
        is EpistemicAgent.AI -> "ai"
        is EpistemicAgent.Computer -> "computer"
    }

}
