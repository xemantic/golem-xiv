/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.ui

import com.xemantic.ai.golem.web.js.ariaLabel
import kotlinx.browser.document
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.dom.create
import kotlinx.html.js.button
import kotlinx.html.js.div
import kotlinx.html.js.span
import kotlinx.html.span
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement

@Suppress("FunctionName")
fun FlowContent.Icon(name: String) {
    span("material-symbols-outlined") {
        +name
    }
}

@Suppress("FunctionName")
fun Icon(
    name: String
): HTMLSpanElement = document.create.span(
    classes = "material-symbols-outlined"
) {
    +name
}

@Suppress("FunctionName")
fun Button(
    label: String,
    icon: String? = null,
    ariaLabel: String
): HTMLButtonElement = document.create.button(
    classes = "action-button"
) {
    if (icon != null) {
        Icon(icon)
    }
    +label
    this.ariaLabel = ariaLabel
}

@Suppress("FunctionName")
fun IconButton(
    icon: String? = null,
    ariaLabel: String
): HTMLButtonElement = document.create.button(
    classes = "icon-button"
) {
    if (icon != null) {
        Icon(icon)
    }
    this.ariaLabel = ariaLabel
}

@Suppress("FunctionName")
fun Link(
    label: String,
    icon: String? = null,
    ariaLabel: String
): HTMLAnchorElement = document.create.a(classes = "navigation-link") {
    if (icon != null) {
        Icon(icon)
    }
    +label
    this.ariaLabel = ariaLabel
} as HTMLAnchorElement

@Suppress("FunctionName")
fun Div(
    classes: String
): HTMLDivElement = document.create.div(classes)
