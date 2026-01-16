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

import com.xemantic.ai.golem.api.CognitionListItem
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.window
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

    private val cognitionListContainer = dom.div(classes = "cognition-list scroll") {}

    private val cognitionListSection = dom.div(classes = "cognition-list-section") {
        div(classes = "small-text bold padding") { +"Past Cognitions" }
        inject(cognitionListContainer)
    }

    private val _cognitionSelections = MutableSharedFlow<Long>()
    override val cognitionSelections: Flow<Long> = _cognitionSelections

    override val element: HTMLElement = dom.nav(classes = "app-navigation-rail left surface-container") {
        ariaLabel = "Main navigation"
        header {
            inject(menuButton)
        }
        inject(
            initiateCognitionItem,
            memoryItem,
            settingsItem,
            cognitionListSection
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

    override fun updateCognitionList(cognitions: List<CognitionListItem>) {
        cognitionListContainer.innerHTML = ""
        cognitions.forEach { cognition ->
            val displayTitle = cognition.title?.takeIf { it.isNotBlank() }
                ?: "Cognition #${cognition.id}"
            val item = dom.a(classes = "row wave") {
                div(classes = "max") { +displayTitle }
            }
            item.onclick = {
                MainScope().launch {
                    _cognitionSelections.emit(cognition.id)
                }
                Unit
            }
            cognitionListContainer.appendChild(item)
        }
    }

}
