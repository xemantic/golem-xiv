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

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.presenter.navigation.NotFoundView
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import com.xemantic.kotlin.js.dom.html.div
import com.xemantic.kotlin.js.dom.node

class HtmlNotFoundView() : NotFoundView, HasRootHtmlElement {

    private val messageDiv = node { div("not-found-message") }

    override var message: String
        get() = messageDiv.innerText
        set(value) {
            messageDiv.innerText = value
        }

    override val element = node {
        div("not-found-page") {
            +messageDiv
        }
    }

}
