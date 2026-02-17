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

package com.xemantic.golem.dom.export

import com.xemantic.markanywhere.flow.semanticEvents
import com.xemantic.markanywhere.test.sameAs
import kotlinx.coroutines.test.runTest
import org.intellij.lang.annotations.Language
import org.w3c.dom.parsing.DOMParser
import kotlin.test.Test

class DomExporterTest {

    @Test
    fun `should export DOM events from simple HTML`() = runTest {
        // given
        @Language("html")
        val html = """
            <html lang="en">
            <head>
            <title>Hello World!</title>
            </head>
            <body>
            <p class="first">
            Hi there
            </p>
            </html>
        """.trimIndent()
        val parser = DOMParser()
        val document = parser.parseFromString(html, type = "text/html")

        // when
        val events = exportSemanticEvents(document).toSemanticEvents()

        // then
        events sameAs semanticEvents(produceTags = true) {
            "html"("lang" to "en") {
                "head" {
                    +"\n"
                    "title" { +"Hello World!" }
                    +"\n"
                }
                +"\n"
                "body" {
                    +"\n"
                    "p"("class" to "first") {
                        +"\nHi there\n"
                    }
                    +"\n"
                }
            }
        }
    }

    @Test
    fun `should detect clickable elements and assign golemIds`() = runTest {
        // given
        @Language("html")
        val html = """
            <html lang="en">
            <head>
            <title>Clickability Test</title>
            </head>
            <body>
            <a href="https://example.com">Link</a>
            <a>Action</a>
            <button>Click me</button>
            <div onclick="doSomething()">Clickable div</div>
            <div role="button">Role button</div>
            <div>Plain div</div>
            <select><option>A</option></select>
            <details><summary>Toggle</summary>Content</details>
            <input type="submit">
            <input type="text">
            <input type="checkbox">
            </body>
            </html>
        """.trimIndent()
        val parser = DOMParser()
        val document = parser.parseFromString(html, type = "text/html")

        // when
        val events = exportSemanticEvents(document).toSemanticEvents()

        // then
        events sameAs semanticEvents(produceTags = true) {
            "html"("lang" to "en") {
                "head" {
                    +"\n"
                    "title" { +"Clickability Test" }
                    +"\n"
                }
                +"\n"
                "body" {
                    +"\n"
                    // <a> with href is self-describing, no golemId
                    "a"("href" to "https://example.com") {
                        +"Link"
                    }
                    +"\n"
                    // <a> without href is clickable
                    "a"("golemId" to "0") {
                        +"Action"
                    }
                    +"\n"
                    // <button> is always clickable
                    "button"("golemId" to "1") {
                        +"Click me"
                    }
                    +"\n"
                    // onclick attribute makes any element clickable
                    "div"("onclick" to "doSomething()", "golemId" to "2") {
                        +"Clickable div"
                    }
                    +"\n"
                    // role="button" makes any element clickable
                    "div"("role" to "button", "golemId" to "3") {
                        +"Role button"
                    }
                    +"\n"
                    // plain <div> is not clickable
                    "div" {
                        +"Plain div"
                    }
                    +"\n"
                    // <select> is always clickable
                    "select"("golemId" to "4") {
                        "option" { +"A" }
                    }
                    +"\n"
                    // <summary> is always clickable
                    "details" {
                        "summary"("golemId" to "5") {
                            +"Toggle"
                        }
                        +"Content"
                    }
                    +"\n"
                    // input type="submit" is clickable
                    "input"("type" to "submit", "golemId" to "6") {}
                    +"\n"
                    // input type="text" is not clickable
                    "input"("type" to "text") {}
                    +"\n"
                    // input type="checkbox" is clickable
                    "input"("type" to "checkbox", "golemId" to "7") {}
                    +"\n\n"
                }
            }
        }
    }

}
