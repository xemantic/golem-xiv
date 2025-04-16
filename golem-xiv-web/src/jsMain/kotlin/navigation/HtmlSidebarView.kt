/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.presenter.Theme
import com.xemantic.ai.golem.presenter.navigation.SidebarView
import com.xemantic.ai.golem.presenter.util.Action
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

    override val element = html.aside("sidebar") {
        div("sidebar-header") {
            h2("Conversation")
            button(classes = "new-chat-btn") {
                icon("plus"); +" New Chat"
            }
        }
        div("sidebar-content")
        div("sidebar-footer")
    }.inject(
        conversationList to ".sidebar-content",
        toggleThemeButton to ".sidebar-footer"
    )

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
