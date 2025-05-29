/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.presenter.Theme
import com.xemantic.ai.golem.presenter.navigation.SidebarView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.inject
import com.xemantic.ai.golem.web.js.resizes
import com.xemantic.ai.golem.web.ui.Button
import com.xemantic.ai.golem.web.ui.Link
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.html.*

class HtmlSidebarView() : SidebarView, HasRootHtmlElement {

    private var theme: Theme = Theme.LIGHT

    private val conversationList = dom.ul("workspace-list") {
        li("no-cognitions") {
            +"No cognitions initiated"
        }
    }

    private val initiateCognitionButton = Button(
        label = "Initiate cognition",
        icon = "network_intel_node",
        ariaLabel = "Initiate cognitive process"
    )

    private val memoryLink = Link(
        label = "Memory",
        icon = "graph_3",
        ariaLabel = "Open memory graph"
    )

    private val themeSwitcher = ThemeSwitcher()

    override val element = dom.aside("sidebar sidebar-hidden") {
        div("sidebar-header") {
            h2("Conversation")
            inject(
                initiateCognitionButton,
                memoryLink
            )
        }
        div("sidebar-content") {
            inject(conversationList)
        }
        div("sidebar-footer") {
            inject(themeSwitcher.element)
        }
    }

    override val memoryActions: Flow<Action> = memoryLink.actions()

    override var opened: Boolean = false
        get() = field
        set(value) {
            field = value
            updateVisibility()
        }

    // TODO should be a separate component
    override fun theme(theme: Theme) {
        themeSwitcher.theme = theme
        this.theme = theme
    }

    private fun updateVisibility() {
        element.classList.remove(
            "sidebar-hidden",
            "sidebar-visible"
        )
        element.classList.add(
            if (!opened) "sidebar-hidden" else "sidebar-visible"
        )
    }

    override val themeChanges: Flow<Theme> = themeSwitcher.actions.map {
        when (theme) {
            Theme.LIGHT -> Theme.DARK
            Theme.DARK -> Theme.LIGHT
        }
    }

    override val resizes: Flow<Action> = window.resizes().map { Action }

}
