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
import com.xemantic.ai.golem.web.js.icon
import com.xemantic.ai.golem.web.util.inject
import com.xemantic.ai.golem.web.view.HtmlView
import kotlinx.coroutines.flow.Flow
import kotlinx.html.*
import kotlinx.html.js.header

class HtmlHeaderView() : HeaderView, HtmlView {

    private val menuButton = html.button(classes = "menu-toggle") {
        ariaLabel = "Toggle sidebar menu"
        icon("bars")
    }

    override val element = html.header {
        nav {
            role = "navigation"
            ariaLabel = "Main navigation"
            div("nav-left")
            div("nav-center") {
                div("logo") {
                    +"Golem XIV"
                }
            }
            div("nav-right") {
                // Empty div to keep the centered title balanced
            }
        }
    }.inject(
        menuButton to ".nav-left"
    )

    override val toggleMenuClicks: Flow<Action> = menuButton.actions()

}
