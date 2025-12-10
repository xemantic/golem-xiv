/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.presenter.environment.Theme
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.js.dom
import com.xemantic.ai.golem.web.js.inject
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.html.li
import kotlinx.html.role
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement

class ThemeSwitcher : HasRootHtmlElement {

    private val themeIcon = document.createElement("i") as HTMLElement

    private val themeLabel = document.createElement("span") as HTMLElement

    override val element = dom.li(classes = "wave round") {
        role = "button"
        ariaLabel = "Theme switcher"
        inject(
            themeIcon,
            themeLabel
        )
    } as HTMLLIElement

    fun themeActionLabel(theme: Theme) {
        when (theme) {
            Theme.LIGHT -> {
                themeIcon.textContent = "light_mode"
                themeLabel.textContent = "Light Mode"
            }
            Theme.DARK -> {
                themeIcon.textContent = "dark_mode"
                themeLabel.textContent = "Dark Mode"
            }
        }
    }

    val themeChanges: Flow<Action> = element.actions()

}