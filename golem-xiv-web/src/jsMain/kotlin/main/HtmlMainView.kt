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

package com.xemantic.ai.golem.web.main

import com.xemantic.ai.golem.presenter.MainView
import com.xemantic.ai.golem.presenter.ScreenView
import com.xemantic.ai.golem.presenter.environment.Theme
import com.xemantic.ai.golem.presenter.cognition.CognitionView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.eventFlow
import com.xemantic.ai.golem.web.navigation.HtmlHeaderView
import com.xemantic.ai.golem.web.navigation.HtmlNavigationRailView
import com.xemantic.ai.golem.web.cognition.HtmlCognitionView
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.html.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

class HtmlMainView(
    private val body: HTMLElement,
    headerView: HtmlHeaderView,
    navigationRailView: HtmlNavigationRailView,
): MainView {

    private val mainElement = dom.main()

    private val overlayElement = dom.div("overlay")

    init {
        body.append(
            navigationRailView.element,
            headerView.element,
            mainElement,
            overlayElement
        )
    }

    override fun theme(theme: Theme) {
        when (theme) {
            Theme.LIGHT -> {
                body.classList.remove("dark")
                body.classList.add("light")
            }
            Theme.DARK -> {
                body.classList.remove("light")
                body.classList.add("dark")
            }
        }
        window.asDynamic().theme = theme.name.lowercase()
    }

    override fun cognitionView(): CognitionView = HtmlCognitionView() // TODO move the factory outside

    override fun display(view: ScreenView) {
        mainElement.innerHTML = ""
        mainElement.append((view as HasRootHtmlElement).element)
    }

    override val resizes: Flow<Action>
        get() = window.eventFlow<Event>("resize").map { Action }

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
