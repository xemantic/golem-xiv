/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.memory

import com.xemantic.ai.golem.presenter.memory.MemoryView
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.iframe
import org.w3c.dom.HTMLElement

class HtmlMemoryView() : MemoryView, HasRootHtmlElement {

    override val element: HTMLElement = document.create.iframe(classes = "memory") {
        src = "neo4j-browser/"
    }

}
