/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.presenter.Theme
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.ui.icon
import com.xemantic.ai.golem.web.util.children
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.html.dom.create
import kotlinx.html.js.button
import kotlinx.html.role
import kotlinx.html.span
import org.w3c.dom.HTMLElement

class ThemeSwitcher : HasRootHtmlElement {

    private val themeIcon = icon("dark_mode")

    private val themeLabel = document.create.span()

    override val element: HTMLElement = document.create.span(classes = "navigation-link") {
        role = "button"
    }.children(
        themeIcon,
        themeLabel
    )

    var theme: Theme = Theme.LIGHT
        get() = field
        set(value) {
            when (value) {
                Theme.LIGHT -> {
                    themeIcon.innerText = "light_mode"
                    themeLabel.textContent = "Light Mode"
                }
                Theme.DARK -> {
                    themeIcon.innerText = "dark_mode"
                    themeLabel.textContent = "Dark Mode"
                }
            }
        }

    val actions: Flow<Action> = element.actions()

}
