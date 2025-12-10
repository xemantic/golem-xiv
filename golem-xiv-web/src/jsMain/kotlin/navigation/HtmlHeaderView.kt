/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.presenter.navigation.HeaderView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.inject
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.coroutines.flow.Flow
import kotlinx.html.*

class HtmlHeaderView() : HeaderView, HasRootHtmlElement {

    private val menuButton = dom.button(classes = "app-menu circle transparent") {
        ariaLabel = "Toggle sidebar menu"
        i { +"menu" }
    }

    override val element = dom.nav(classes = "app-navigation-bar top left-align surface-container") {
        inject(menuButton)
        h6(classes = "max center-align") {
            +"Golem XIV"
        }
    }

    override val toggleMenuClicks: Flow<Action> = menuButton.actions()

}