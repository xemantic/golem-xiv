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

package com.xemantic.ai.golem.web.main

import com.xemantic.ai.golem.presenter.MainView
import com.xemantic.ai.golem.presenter.Theme
import com.xemantic.ai.golem.presenter.context.ContextView
import com.xemantic.ai.golem.web.context.HtmlContextView
import com.xemantic.ai.golem.web.navigation.HtmlHeaderView
import com.xemantic.ai.golem.web.navigation.HtmlSidebarView
import com.xemantic.ai.golem.web.view.HtmlView
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.html.*
import kotlinx.html.dom.create
import org.w3c.dom.HTMLElement

class HtmlMainView(
    private val body: HTMLElement,
    headerView: HtmlHeaderView,
    sidebarView: HtmlSidebarView,
): MainView {

    private val sidebarView = HtmlSidebarView()

    private val contextView = HtmlContextView()

    private val mainElement = document.create.main()

    private val overlayElement = document.create.div("overlay")

    init {
        val mainContainer = document.create.div("main-container")
        val mainElement = document.create.main()
        mainElement.append(contextView.element)
//        // TODO remove welcome-text
        mainContainer.append(
            headerView.element,
            mainElement
        )
        body.append(
            sidebarView.element,
            mainContainer,
            overlayElement
        )
        val savedTheme = localStorage.getItem("theme")?.let {
            Theme.valueOf(it)
        } ?: Theme.LIGHT
        theme(savedTheme)
    }

    override fun theme(theme: Theme) {
        when (theme) {
            Theme.LIGHT -> {
                body.classList.toggle("dark-theme", false)
                localStorage.setItem("theme", theme.name)
            }
            Theme.DARK -> {
                body.classList.toggle("dark-theme", true)
                localStorage.setItem("theme", theme.name)
            }
        }
        sidebarView.theme(theme)
    }

    override fun contextView(): ContextView = HtmlContextView()

    override fun displayContext(view: ContextView) {
        mainElement.innerHTML = ""
        mainElement.append()
        mainElement.append((view as HtmlView).element)
    }

}

//private fun HTMLElement.buildMainUi(
//    mainElement: HTMLElement
//) = append {
//
//    header {
//        h1("Golem XIV")
//
//        div(classes = "user-controls") {
//            button(classes = "settings-button") {
//                attributes["aria-label"] = "Settings"
//                attributes["type"] = "button"
//                span(classes = "icon") { +"⚙️" }
//                //onClickFunction = { openSettingsDialog() }
//            }
//
//            div(classes = "user-menu") {
//                button {
//                    attributes["aria-haspopup"] = "menu"
//                    attributes["aria-expanded"] = "false"
//                    img(classes = "avatar") {
//                        src = "/api/placeholder/32/32"
//                        alt = "User profile"
//                    }
//                }
//            }
//        }
//    }
//
//    append(mainElement)
//
//    footer {
//        +"© 2025 Xemantic"
//    }

//}
