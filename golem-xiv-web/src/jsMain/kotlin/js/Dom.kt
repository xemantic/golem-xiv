/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
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

package com.xemantic.ai.golem.web.js

import kotlinx.browser.document
import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.id
import org.w3c.dom.HTMLElement

val dom: TagConsumer<HTMLElement> get() = GolemTagConsumer()

fun FlowContent.inject(vararg elements: HTMLElement) {
    elements.forEach {
        inject(it)
    }
}

fun FlowContent.inject(element: HTMLElement) {
    if (consumer is GolemTagConsumer) {
        val golemTagConsumer = consumer as GolemTagConsumer
        val injectionId = "golem-marker-${golemTagConsumer.markerIndex++}"
        golemTagConsumer.injectionPoints[injectionId] = element
        div {
            id = injectionId
        }
    } else {
        throw IllegalStateException(
            "TagConsumer must be GolemTagConsumer, use com.xemantic.ai.golem.web.js.dom"
        )
    }
}

private class GolemTagConsumer(
    private val consumer: TagConsumer<HTMLElement> = document.create
) : TagConsumer<HTMLElement> by consumer {

    var markerIndex = 0

    var injectionPoints = mutableMapOf<String, HTMLElement>()

    override fun finalize(): HTMLElement {
        val final = consumer.finalize()
        injectionPoints.forEach { (marker, node) ->
            val element = final.querySelector("#$marker")
            element!!.replaceWith(node)
        }
        injectionPoints.clear()
        return final
    }

}
