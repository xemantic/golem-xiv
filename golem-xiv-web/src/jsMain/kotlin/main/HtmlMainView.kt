/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.main

import com.xemantic.ai.golem.presenter.MainView
import com.xemantic.ai.golem.presenter.ScreenView
import com.xemantic.ai.golem.presenter.environment.Theme
import com.xemantic.ai.golem.presenter.phenomena.CognitiveWorkspaceView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.eventFlow
import com.xemantic.ai.golem.web.navigation.HtmlHeaderView
import com.xemantic.ai.golem.web.navigation.HtmlSidebarView
import com.xemantic.ai.golem.web.workspace.HtmlCognitiveWorkspaceView
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
    sidebarView: HtmlSidebarView,
): MainView {

    private val mainElement = dom.main()

    private val overlayElement = dom.div("overlay")

    init {
        body.append(
            headerView.element,
            sidebarView.element,
            mainElement,
            overlayElement
        )
    }

    override fun theme(theme: Theme) {
        when (theme) {
            Theme.LIGHT -> body.classList.remove("dark-theme")
            Theme.DARK -> body.classList.add("dark-theme")
        }
    }

    override fun workspaceView(): CognitiveWorkspaceView = HtmlCognitiveWorkspaceView() // TODO move the factory outside

    override fun display(view: ScreenView) {
        mainElement.innerHTML = ""
        mainElement.append((view as HasRootHtmlElement).element)
    }

    override val workspaceSelection: Flow<String> = window.eventFlow<Event>(
        "hashchange"
    ).map {
        window.location.hash // TODO more parsing here?
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
