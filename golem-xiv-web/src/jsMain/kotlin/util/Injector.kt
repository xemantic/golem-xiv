/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.util

import org.w3c.dom.HTMLElement

fun <T : HTMLElement> T.inject(
    vararg injections: Pair<HTMLElement, String>,
): T {
    injections.forEach { (element, selector) ->
        querySelector(selector)?.append(element) ?: throw IllegalArgumentException(
            "Selector '$selector' did not match any element."
        )
    }
    return this
}

fun <T : HTMLElement> T.children(
    vararg elements: HTMLElement
): HTMLElement = also {
    elements.forEach {
        append(it)
    }
}


fun <T : HTMLElement> T.appendTo(
    selector: String,
    vararg elements: HTMLElement
): HTMLElement = also {
    querySelector(selector)?.run {
        elements.forEach {
            this@run.append(it)
        }
    } ?: throw IllegalArgumentException(
        "Selector '$selector' did not match any element."
    )
}
