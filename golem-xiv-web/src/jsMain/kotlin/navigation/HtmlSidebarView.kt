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
import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.js.clicks
import com.xemantic.ai.golem.web.js.icon
import com.xemantic.ai.golem.web.js.resizes
import com.xemantic.ai.golem.web.util.inject
import com.xemantic.ai.golem.web.view.HtmlView
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.html.*

class HtmlSidebarView() : SidebarView, HtmlView {

    private var theme: Theme = Theme.LIGHT

    private val conversationList = html.ul("conversation-list") {
        li("no-conversations") {
            +"No conversations yet"
        }
    }

    private val themeIcon = html.i("fas fa-moon")

    private val themeLabel = html.span {
        +"Toggle Theme"
    }

    private val toggleThemeButton = html.button(
        classes = "theme-toggle"
    ) {
        ariaLabel = "Toggle dark/light theme"
    }.apply {
        append(
            themeIcon,
            themeLabel
        )
    }

    private val knowledgeGraphButton = html.button(classes = "new-chat-btn") {
        icon("database"); +" Knowledge Graph"
    }

    override val element = html.aside("sidebar sidebar-hidden") {
        div("sidebar-header") {
            h2("Conversation")
            button(classes = "new-chat-btn") {
                icon("plus"); +" New Cognitive Workspace"
            }
        }
        div("sidebar-content")
        div("sidebar-footer")
    }.inject(
        knowledgeGraphButton to ".sidebar-header",
        conversationList to ".sidebar-content",
        toggleThemeButton to ".sidebar-footer"
    )

    override val memoryActions: Flow<Action> = knowledgeGraphButton.actions()

    override var opened: Boolean = false
        get() = field
        set(value) {
            field = value
            updateVisibility()
        }

    override fun theme(theme: Theme) {
        when (theme) {
            Theme.LIGHT -> {
                themeIcon.className = "fas fa-sun"
                themeLabel.textContent = "Light Mode"
            }
            Theme.DARK -> {
                themeIcon.className = "fas fa-moon"
                themeLabel.textContent = "Dark Mode"
            }
        }
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

    override val themeChanges: Flow<Theme> = toggleThemeButton.clicks().map {
        when (theme) {
            Theme.LIGHT -> Theme.DARK
            Theme.DARK -> Theme.LIGHT
        }
    }

    override val resizes: Flow<Action> = window.resizes().map { Action }

}
