/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
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

package com.xemantic.golem.web.app

import com.xemantic.golem.viewmodel.app.AppViewModel
import com.xemantic.golem.viewmodel.navigation.NavigationStrings
import com.xemantic.golem.web.ElementBuilder
import com.xemantic.golem.web.navigation.navigationDrawerView
import com.xemantic.golem.web.navigation.navigationRailView
import com.xemantic.kotlin.js.dom.element.minusAssign
import com.xemantic.kotlin.js.dom.element.plusAssign
import kotlinx.browser.window
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.HTMLElement

fun ElementBuilder.appView(
    viewModel: AppViewModel,
    headerElement: HTMLElement,
    mainElement: HTMLElement,
    strings: NavigationStrings
) {

    navigationRailView(viewModel.navigationViewModel, strings)
    navigationDrawerView(viewModel.navigationViewModel, strings)

    +headerElement
    +mainElement

    // the shader is reading theme from window property, so we need a generic logic setting it up
    viewModel.navigationViewModel.theme.onEach { theme ->
        when (theme) {
            LIGHT -> {
                node -= "dark"
                node += "light"
            }
            DARK -> {
                node -= "light"
                node += "dark"
            }
        }
        window.asDynamic().theme = theme.name.lowercase()
    }.launchIn(viewModel.scope)
}
