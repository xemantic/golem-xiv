/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.presenter.environment.Theme
import com.xemantic.ai.golem.presenter.navigation.SidebarView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.js.clicks
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.inject
import com.xemantic.ai.golem.web.js.resizes
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.html.*
import org.w3c.dom.HTMLElement

class HtmlNavigationRailView : SidebarView, HasRootHtmlElement {

    private val initiateCognitionItem = dom.a {
        attributes["data-target"] = "initiate-cognition"
        ariaLabel = "Initiate cognitive process"
        i { +"network_intel_node" }
        div { +"Initiate" }
    }

    private val memoryItem = dom.a {
        attributes["data-target"] = "memory"
        ariaLabel = "Open memory graph"
        i { +"graph_3" }
        div { +"Memory" }
    }

    private val settingsItem = dom.a {
        attributes["data-target"] = "settings"
        ariaLabel = "Settings"
        i { +"settings" }
        div { +"Settings" }
    }

    private val menuButton = dom.button(classes = "app-menu extra circle transparent") {
        ariaLabel = "Menu"
        i { +"menu" }
    }

    private val themeSwitcherButton = dom.button(classes = "app-theme-switcher extra circle transparent") {
        ariaLabel = "Theme switcher"
        i { +"light_mode" }
    }

    override val element: HTMLElement = dom.nav(classes = "app-navigation-rail left surface-container") {
        ariaLabel = "Main navigation"
        header {
            inject(menuButton)
        }
        inject(
            initiateCognitionItem,
            memoryItem,
            settingsItem
        )
        div(classes = "max")
        inject(themeSwitcherButton)
    }

    private val menuIcon: HTMLElement?
        get() = menuButton.querySelector("i") as? HTMLElement

    init {
        menuButton.clicks().onEach {
            element.classList.toggle("max")
            menuIcon?.textContent = if (element.classList.contains("max")) "menu_open" else "menu"
        }.launchIn(MainScope())
    }

    override val initiateCognitionActions: Flow<Action> = initiateCognitionItem.actions()

    override val memoryActions: Flow<Action> = memoryItem.actions()

    override val themeChanges: Flow<Action> = themeSwitcherButton.actions()

    override val resizes: Flow<Action> = window.resizes().map { Action }

    override var opened: Boolean = false
        set(value) {
            field = value
            // Navigation rail doesn't open/close like a drawer, it's always visible
        }

    override fun themeActionLabel(theme: Theme) {
        val icon = themeSwitcherButton.querySelector("i")
        icon?.textContent = when (theme) {
            Theme.LIGHT -> "light_mode"
            Theme.DARK -> "dark_mode"
        }
    }

}
