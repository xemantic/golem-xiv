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
import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.js.eventFlow
import com.xemantic.ai.golem.web.view.HtmlView
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.html.*
import kotlinx.html.dom.create
import org.w3c.dom.events.MouseEvent

class HtmlSidebarView() : SidebarView, HtmlView {

    private var theme: Theme = Theme.LIGHT

    private val conversationList = document.create.ul("conversation-list") {
        li("no-conversations") {
            +"No conversations yet"
        }
    }

    private val themeIcon = document.create.i("fas fa-moon")

    private val themeLabel = document.create.span {
        +"Toggle Theme"
    }

    private val toggleThemeButton = document.create.button {
        ariaLabel = "Toggle dark/light theme"
    }.apply {
        append(
            themeIcon,
            themeLabel
        )
    }

    override val element = document.create.aside("sidebar") {
        div("sidebar-header") {
            h2("Conversation")
            button(classes = "new-chat-btn") {
                i("fas fa-plus"); +" New Chat"
            }
        }
        div("sidebar-content")
        div("sidebar-footer")
    }.apply {
        querySelector(".sidebar-content")!!.append(conversationList)
        querySelector(".sidebar-footer")!!.append(toggleThemeButton)
    }

    override var opened: Boolean = false
        get() = field
        set(value) {
            field = value
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

    override val themeChanges: Flow<Theme> = toggleThemeButton.eventFlow<MouseEvent>(
        "click"
    ).map {
        when (theme) {
            Theme.LIGHT -> Theme.DARK
            Theme.DARK -> Theme.LIGHT
        }
    }

    // TODO attach this
    private fun handleWindowResize() {
        // Update sidebar positioning based on screen size
        if (window.innerWidth <= 768) {
            // Mobile view
            element.classList.remove("sidebar-hidden-desktop", "sidebar-visible")

            if (!opened) {
                element.classList.add("sidebar-hidden-mobile")
            } else {
                element.classList.add("sidebar-visible")
            }
        } else {
            // Desktop view
            element.classList.remove("sidebar-hidden-mobile", "sidebar-visible")

            if (!opened) {
                element.classList.add("sidebar-hidden-desktop")
            } else {
                element.classList.add("sidebar-visible")
            }
        }

        // Close sidebar automatically when resizing to mobile view if it's open
        if (window.innerWidth <= 768 && opened) {
            //toggleSidebar()
        }
    }

}
