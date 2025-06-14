/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.presenter.navigation.NotFoundView
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.inject
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.html.js.div

class HtmlNotFoundView() : NotFoundView, HasRootHtmlElement {

    private val messageDiv = dom.div("not-found-message")

    override var message: String
        get() = messageDiv.innerText
        set(value) {
            messageDiv.innerText = value
        }

    override val element = dom.div("not-found-page") {
        inject(
            messageDiv
        )
    }

}
