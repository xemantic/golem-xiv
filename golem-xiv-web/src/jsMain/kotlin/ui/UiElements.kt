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

package com.xemantic.ai.golem.web.ui

import com.xemantic.kotlin.js.dom.ariaLabel
import com.xemantic.kotlin.js.dom.html.*
import com.xemantic.kotlin.js.dom.node
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement

@Suppress("FunctionName")
fun Button(
    label: String,
    icon: String? = null,
    ariaLabel: String
): HTMLButtonElement = node {
    button("round") {
        if (icon != null) {
            icon(icon)
        }
        +label
        it.ariaLabel = ariaLabel
    }
}

@Suppress("FunctionName")
fun IconButton(
    icon: String? = null,
    ariaLabel: String
): HTMLButtonElement = node {
    button("circle transparent") {
        if (icon != null) {
            icon(icon)
        }
        it.ariaLabel = ariaLabel
    }
}

@Suppress("FunctionName")
fun Link(
    label: String,
    icon: String? = null,
    ariaLabel: String
): HTMLAnchorElement = node {
    a("wave round") {
        if (icon != null) {
            icon(icon)
        }
        +label
        it.ariaLabel = ariaLabel
    }
}

@Suppress("FunctionName")
fun Div(classes: String): HTMLDivElement = node {
    div(classes)
}
