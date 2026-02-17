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

import com.xemantic.kotlin.js.JsObject
import com.xemantic.kotlin.js.collections.*
import com.xemantic.kotlin.js.dom.forEach
import com.xemantic.kotlin.js.globalThis
import com.xemantic.kotlin.js.set
import org.w3c.dom.*
import kotlin.js.collections.JsArray
import kotlin.js.collections.JsMap

private var nextGolemId = 0

private const val GOLEM_ELEMENTS_KEY = "__golemElements"

@JsExport
fun exportSemanticEvents(document: Document): String {
    nextGolemId = 0
    val events = jsArrayOf<Event>()
    val registry = JsMap<String, Element>()
    globalThis[GOLEM_ELEMENTS_KEY] = registry
    document.childNodes.traverse(events, registry)
    return events.map { event ->
        JSON.stringify(event)
    }.join("\n")
}

@JsExport
fun getElementByGolemId(
    golemId: String
): Element? = globalThis[GOLEM_ELEMENTS_KEY][golemId]

private fun Node.traverse(
    events: JsArray<Event>,
    registry: JsMap<String, Element>
) {
    when (this) {

        is Element -> {
            val name = localName
            val clickable = isClickable()
            val attributes = extractAttributes()
            if (clickable) {
                val golemId = "${nextGolemId++}"
                attributes["golemId"] = golemId
                registry[golemId] = this
            }
            events += MarkEvent(name, attributes = attributes)
            childNodes.traverse(events, registry)
            events += UnmarkEvent(name)
        }

        is Text -> {
            textContent?.let { text ->
                events += TextEvent(text)
            }
        }

    }
}

private fun NodeList.traverse(
    events: JsArray<Event>,
    registry: JsMap<String, Element>
) {
    forEach {
        it.traverse(events, registry)
    }
}

private val clickableElements = jsSetOf("a", "button", "select", "summary")
private val formInputs = jsSetOf("button", "submit", "reset", "checkbox", "radio")

private fun Element.isClickable(): Boolean {
    // <a> with href excluded - action is obvious from the href
    if (localName == "a" && hasAttribute("href")) return false
    return when (localName) {
        in clickableElements -> true
        "input" -> {
            val type = getAttribute("type")?.lowercase() ?: "text"
            type in formInputs
        }
        else ->
            hasAttribute("onclick")
                || getAttribute("role") == "button"
                || ownerDocument?.defaultView
                    ?.getComputedStyle(this)
                    ?.getPropertyValue("cursor") == "pointer"
    }
}

private fun Element.extractAttributes() = JsObject().also { obj ->
    attributes.forEach {
        obj[it.name] = it.value
    }
}
