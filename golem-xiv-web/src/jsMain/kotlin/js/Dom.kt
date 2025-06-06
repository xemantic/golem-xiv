/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
