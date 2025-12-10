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
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.inject
import com.xemantic.ai.golem.web.js.resizes
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.html.*
import org.w3c.dom.HTMLDialogElement

class HtmlSidebarView() : SidebarView, HasRootHtmlElement {

    private val cognitionsItem = dom.li(classes = "wave round") {
        role = "button"
        ariaLabel = "Cognition overview"
        i { +"analytics" }
        span { +"Cognitions" }
    }

    private val initiateCognitionItem = dom.li(classes = "wave round") {
        role = "button"
        ariaLabel = "Initiate cognitive process"
        i { +"network_intel_node" }
        span { +"Initiate cognition" }
    }

    private val memoryItem = dom.li(classes = "wave round") {
        role = "button"
        ariaLabel = "Open memory graph"
        i { +"graph_3" }
        span { +"Memory" }
    }

    private val settingsItem = dom.li(classes = "wave round") {
        role = "button"
        ariaLabel = "Settings"
        i { +"settings" }
        span { +"Settings" }
    }

    private val themeSwitcher = ThemeSwitcher()

    private val menuCloseButton = dom.button(classes = "app-menu circle transparent") {
        ariaLabel = "Close menu"
        i { +"menu_open" }
    }

    override val element = dom.dialog(classes = "app-navigation-drawer left") {
        role = "navigation"
        ariaLabel = "Main navigation"
        header {
            nav {
                inject(menuCloseButton)
                h6(classes = "max") { +"Golem XIV" }
            }
        }
        div(classes = "space")
        ul(classes = "list") {
            inject(
                cognitionsItem,
                initiateCognitionItem,
                memoryItem,
                settingsItem
            )
        }
        div(classes = "max")
        ul(classes = "list") {
            inject(themeSwitcher.element)
        }
    } as HTMLDialogElement

    override val memoryActions: Flow<Action> = memoryItem.actions()

    override var opened: Boolean = false
        get() = field
        set(value) {
            field = value
            updateVisibility()
        }

    override fun themeActionLabel(theme: Theme) {
        themeSwitcher.themeActionLabel(theme)
    }

    private fun updateVisibility() {
        if (opened) {
            element.show()
        } else {
            element.close()
        }
    }

    override val initiateCognitionActions: Flow<Action> = initiateCognitionItem.actions()

    override val themeChanges: Flow<Action> = themeSwitcher.themeChanges

    override val resizes: Flow<Action> = window.resizes().map { Action }

    val closeMenuClicks: Flow<Action> = menuCloseButton.actions()

}