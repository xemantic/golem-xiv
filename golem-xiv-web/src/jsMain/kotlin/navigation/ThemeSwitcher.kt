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
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.inject
import com.xemantic.ai.golem.web.ui.Icon
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.coroutines.flow.Flow
import kotlinx.html.role
import kotlinx.html.span
import org.w3c.dom.HTMLElement

class ThemeSwitcher : HasRootHtmlElement {

    private val themeIcon = Icon("dark_mode")

    private val themeLabel = dom.span()

    override val element: HTMLElement = dom.span(classes = "navigation-link") {
        role = "button"
        inject(
            themeIcon,
            themeLabel
        )
    }

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
