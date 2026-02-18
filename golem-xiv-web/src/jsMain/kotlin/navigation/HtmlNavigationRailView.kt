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

import com.xemantic.ai.golem.presenter.environment.Theme
import com.xemantic.ai.golem.presenter.navigation.SidebarView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.clicks
import com.xemantic.ai.golem.web.js.eventFlow
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import com.xemantic.kotlin.js.dom.ariaLabel
import com.xemantic.kotlin.js.dom.html.*
import com.xemantic.kotlin.js.dom.node
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

class HtmlNavigationRailView : SidebarView, HasRootHtmlElement {

    private val initiateCognitionItem = node {
        a {
            it.dataset["target"] = "initiate-cognition"
            it.ariaLabel = "Initiate cognitive process"
            icon("network_intel_node")
            div { +"Initiate" }
        }
    }

    private val memoryItem = node {
        a {
            it.dataset["target"] = "memory"
            it.ariaLabel = "Open memory graph"
            icon("graph_3")
            div { +"Memory" }
        }
    }

    private val settingsItem = node {
        a {
            it.dataset["target"] = "settings"
            it.ariaLabel = "Settings"
            icon("settings")
            div { +"Settings" }
        }
    }

    private val menuButton = node {
        button("app-menu extra circle transparent") {
            it.ariaLabel = "Menu"
            icon("menu")
        }
    }

    private val themeSwitcherButton = node {
        button("app-theme-switcher extra circle transparent") {
            it.ariaLabel = "Theme switcher"
            icon("light_mode")
        }
    }

    override val element: HTMLElement = node {
        nav("app-navigation-rail left surface-container") {
            it.ariaLabel = "Main navigation"
            header {
                +menuButton
            }
            +initiateCognitionItem
            +memoryItem
            +settingsItem
            div("max")
            +themeSwitcherButton
        }
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

    override val resizes: Flow<Action> = window.eventFlow<Event>("resize").map { Action }

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
