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

import com.xemantic.ai.golem.presenter.navigation.HeaderView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import com.xemantic.kotlin.js.dom.ariaLabel
import com.xemantic.kotlin.js.dom.html.*
import com.xemantic.kotlin.js.dom.node
import kotlinx.coroutines.flow.Flow

class HtmlHeaderView() : HeaderView, HasRootHtmlElement {

    private val menuButton = node {
        button("app-menu circle transparent") {
            it.ariaLabel = "Toggle sidebar menu"
            icon("menu")
        }
    }

    override val element = node {
        nav("app-navigation-bar top s left-align") {
            +menuButton
        }
    }

    override val toggleMenuClicks: Flow<Action> = menuButton.actions()

}
