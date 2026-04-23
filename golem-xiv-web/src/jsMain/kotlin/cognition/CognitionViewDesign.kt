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

package com.xemantic.golem.web.cognition

import com.xemantic.golem.viewmodel.cognition.CognitionViewModel
import com.xemantic.golem.viewmodel.navigation.Navigation
import com.xemantic.golem.viewmodel.navigation.NavigationStrings
import com.xemantic.golem.viewmodel.prompt.PromptViewModel
import com.xemantic.golem.web.ElementBuilder
import com.xemantic.golem.web.prompt.promptView
import com.xemantic.kotlin.js.dom.event.onInput
import com.xemantic.kotlin.js.dom.html.*
import kotlin.random.Random

fun ElementBuilder.cognitionViewDesign(
    viewModel: CognitionViewModel,
    navigation: Navigation,
    strings: NavigationStrings,
) {
    //cognitionsView(viewModel, navigation, strings)
    cognitionDesign()
}

private fun ElementBuilder.cognitionSelectorDesign() = section("x-cognition-selector") {
    header {
        nav {
            div("field large prefix round fill active max") {
                icon("search")
                input(
                    type = "text",
                    placeholder = "Search cognitions",
                ) {
                    onInput {

                    }
                }
                icon("close")
            }
            button("x-add-cognition circle extra") {
                icon("add").apply { className = "extra" } // TODO update with new API
            }
        }
    }
    div("x-cognition-list scroll") {
        ul("list border") {
            repeat(30) { cognitionIndex ->
                li {
                    progress("circle indeterminate small",
                        value = Random.nextDouble(10.0, 90.0),
                        max = 100.0
                    )
                    div("max") {
                        h6("small") {
                            +"Cognition $cognitionIndex"
                        }
                    }
                    label { +">" }
                }
            }
        }
    }
    footer("fixed") {
        nav {
            div { +"16356 / $234.234" }
        }
    }
}


private fun ElementBuilder.cognitionDesign() = section("x-cognition") {



    article("x-enunciation border") {
        details {
            summary("wave") {
                nav {
                    icon("person")
                    +"morisil"
                    div("max")
                    +"01-01-2026 12:11:01"
                    icon("unfold_more")
                }
            }
            div {
                +"Actant:"
            }
        }
        div("x-phenomenon") {
            p {
                +"Calculate Fibonacci number 42"
            }
        }
    }
    article("x-enunciation border") {
        details {
            summary("wave") {
                nav {
                    icon("smart_toy")
                    +"Golem XIV"
                    div("max")
                    +"01-01-2026 12:11:02"
                    icon("unfold_more")
                }
            }
            div {
                table("stripes right-align no-space") {
                    tr {
                        td {
                            +"Actant"
                        }
                        td("left-align") {
                            +"Self"
                        }
                    }
                    tr {
                        td {
                            +"cognizer"
                        }
                        td("left-align") {
                            +"Claude Opus 4.6"
                        }
                    }
                    tr {
                        td {
                            +"temperature"
                        }
                        td("left-align") {
                            +"1"
                        }
                    }
                }
            }
        }
        div("x-phenomenon") {
            p {
                +"I will use GolemScript to calculate 42th Fibonacci number 42"
            }
        }
        div("x-phenomenon x-phenomenon-intent") {
            details("border small-padding") {
                summary {
                    nav {
                        icon("code_blocks")
                        +"Calculate 42th Fibonacci number 42"
                        div("max")
                        icon("unfold_more")
                    }
                }
                pre("x-code-kotlin") {
                    code {
                        +$$"""
                        fun fibonacci(n: Int): Long {
                            if (n <= 1) return n.toLong()
                            var a = 0L
                            var b = 1L
                            for (i in 2..n) {
                                val temp = a + b
                                a = b
                                b = temp
                            }
                            return b
                        }

                        val result = fibonacci(42)
                        "Fibonacci(42) = $result"
                    """.trimIndent()
                    }
                }
            }
        }
    }
    article("x-enunciation border") {
        details {
            summary {
                nav {
                    icon("computer")
                    +"morisil's computer"
                    div("max")
                    +"01-01-2026 12:11:03"
                    icon("unfold_more")
                }
            }
            div {
                +"Actant: Mac, OS ..."
            }
        }
        div("x-phenomenon x-phenomenon-fulfillment") {
            details("border small-padding") {
                summary {
                    nav {
                        icon("article")
                        +"Calculation result"
                        div("max")
                        icon("unfold_more")
                    }
                }
                pre {
                    code {
                        +"Fibonacci(42) = 267914296"
                    }
                }
            }
        }
    }
    article("x-enunciation border") {
        details {
            summary {
                nav {
                    icon("smart_toy")
                    +"Golem XIV"
                    div("max")
                    +"01-01-2026 12:11:04"
                    icon("unfold_more")
                }
            }
            div {
                +"Actant:"
            }
        }
        div("x-phenomenon x-phenomenon-text") {
            p {
                +"The 42nd Fibonacci number is **267,914,296**."
            }
            p {
                +"For reference, the Fibonacci sequence starts with 0, 1, 1, 2, 3, 5, 8, 13, 21, ... where each number is the sum of the two preceding ones. So F(0) = 0, F(1) = 1, and F(42) = **267,914,296**."
            }
        }
    }

    article("x-enunciation border") {
        details {
            summary {
                nav {
                    icon("person")
                    +"morisil"
                    div("max")
                    +"01-01-2026 12:11:06"
                    icon("unfold_more")
                }
            }
            div {
                +"Actant:"
            }
        }
        div("x-phenomenon") {
            p {
                +"Do it wit a recursive agent"
            }
        }
    }

    article("x-enunciation border") {
        details {
            summary {
                nav {
                    icon("smart_toy")
                    +"Golem XIV"
                    div("max")
                    +"01-01-2026 12:11:07"
                    icon("unfold_more")
                }
            }
            div {
                table("stripes right-align no-space") {
                    tr {
                        td {
                            +"cognizer"
                        }
                        td {
                            +"Claude Opus 4.6"
                        }
                    }
                    tr {
                        td {
                            +"temperature"
                        }
                        td {
                            +"1"
                        }
                    }
                }
            }
        }

        div("x-phenomenon x-phenomenon-invocation") {
            details("border small-padding") {
                summary {
                    nav {
                        icon("exit_to_app")
                        +"Calculate Fibonacci number 42 in a subagent"
                        div("max")
                        icon("unfold_more")
                    }
                }
                pre("x-code-kotlin") {
                    code {
                        +"""
                            invoke("Calculate 42nd Fibonacci number")
                        """.trimIndent()
                    }
                }
            }
        }
        div("x-subagent") {
            article("x-enunciation border") {
                details {
                    summary {
                        nav {
                            icon("person")
                            +"morisil"
                            div("max")
                            +"01-01-2026 12:11:01"
                            icon("unfold_more")
                        }
                    }
                    div {
                        +"Actant:"
                    }
                }
                div("x-phenomenon") {
                    p {
                        +"Calculate Fibonacci number 42"
                    }
                }
            }
            article("x-enunciation border") {
                details {
                    summary {
                        nav {
                            icon("smart_toy")
                            +"Golem XIV"
                            div("max")
                            +"01-01-2026 12:11:02"
                            icon("unfold_more")
                        }
                    }
                    div {
                        table("stripes right-align no-space") {
                            tr {
                                td {
                                    +"cognizer"
                                }
                                td {
                                    +"Claude Opus 4.6"
                                }
                            }
                            tr {
                                td {
                                    +"temperature"
                                }
                                td {
                                    +"1"
                                }
                            }
                        }
                    }
                }
                div("x-phenomenon") {
                    p {
                        +"I will use GolemScript to calculate 42th Fibonacci number 42"
                    }
                }
                div("x-phenomenon x-phenomenon-intent") {
                    details("border small-padding") {
                        summary {
                            nav {
                                icon("code_blocks")
                                +"Calculate 42th Fibonacci number 42"
                                div("max")
                                icon("unfold_more")
                            }
                        }
                        pre("x-code-kotlin") {
                            code {
                                +$$"""
                        fun fibonacci(n: Int): Long {
                            if (n <= 1) return n.toLong()
                            var a = 0L
                            var b = 1L
                            for (i in 2..n) {
                                val temp = a + b
                                a = b
                                b = temp
                            }
                            return b
                        }

                        val result = fibonacci(42)
                        "Fibonacci(42) = $result"
                    """.trimIndent()
                            }
                        }
                    }
                }
            }
            article("x-enunciation border") {
                details {
                    summary {
                        nav {
                            icon("computer")
                            +"morisil's computer"
                            div("max")
                            +"01-01-2026 12:11:03"
                            icon("unfold_more")
                        }
                    }
                    div {
                        +"Actant: Mac, OS ..."
                    }
                }
                div("x-phenomenon x-phenomenon-fulfillment") {
                    details("border small-padding") {
                        summary {
                            nav {
                                icon("article")
                                +"Calculation result"
                                div("max")
                                icon("unfold_more")
                            }
                        }
                        pre {
                            code {
                                +"Fibonacci(42) = 267914296"
                            }
                        }
                    }
                }
            }
            article("x-enunciation border") {
                details {
                    summary {
                        nav {
                            icon("smart_toy")
                            +"Golem XIV"
                            div("max")
                            +"01-01-2026 12:11:04"
                            icon("unfold_more")
                        }
                    }
                    div {
                        +"Actant:"
                    }
                }
                div("x-phenomenon x-phenomenon-text") {
                    p {
                        +"The 42nd Fibonacci number is **267,914,296**."
                    }
                    p {
                        +"For reference, the Fibonacci sequence starts with 0, 1, 1, 2, 3, 5, 8, 13, 21, ... where each number is the sum of the two preceding ones. So F(0) = 0, F(1) = 1, and F(42) = **267,914,296**."
                    }
                }
            }
        }
    }

    promptView(PromptViewModel())

}
