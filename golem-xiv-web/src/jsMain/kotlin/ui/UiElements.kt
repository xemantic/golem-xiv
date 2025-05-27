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
import org.w3c.dom.HTMLSpanElement

fun FlowContent.icon(name: String) {
    span("material-symbols-outlined") {
        +name
    }
}

fun icon(
    name: String
): HTMLSpanElement = document.create.span(
    classes = "material-symbols-outlined"
) {
    +name
}

fun button(
    label: String,
    icon: String? = null,
    ariaLabel: String
): HTMLButtonElement = document.create.button(
    classes = "action-button"
) {
    if (icon != null) {
        icon(icon)
    }
    +label
    this.ariaLabel = ariaLabel
}

fun iconButton(
    icon: String? = null,
    ariaLabel: String
): HTMLButtonElement = document.create.button(
    classes = "icon-button"
) {
    if (icon != null) {
        icon(icon)
    }
    this.ariaLabel = ariaLabel
}

fun link(
    label: String,
    icon: String? = null,
    ariaLabel: String
): HTMLAnchorElement = document.create.a(classes = "navigation-link") {
    if (icon != null) {
        icon(icon)
    }
    +label
    this.ariaLabel = ariaLabel
} as HTMLAnchorElement

fun div(classes: String) = document.create.div(classes = classes)
