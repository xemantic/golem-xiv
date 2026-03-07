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
import com.xemantic.golem.web.navigation.headerView
import com.xemantic.golem.web.navigation.navigationDrawerView
import com.xemantic.golem.web.navigation.navigationRailView
import com.xemantic.kotlin.js.dom.NodeBuilder
import com.xemantic.kotlin.js.dom.element.minusAssign
import com.xemantic.kotlin.js.dom.element.plusAssign
import com.xemantic.kotlin.js.dom.html.main
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.dom.clear
import org.w3c.dom.HTMLElement

fun NodeBuilder.appView(
    viewModel: AppViewModel,
    screenFlow: Flow<HTMLElement>
) {

    navigationRailView(viewModel.navigationViewModel)
    headerView(viewModel.navigationViewModel)
    navigationDrawerView(viewModel.navigationViewModel)

    main { main ->
        screenFlow.onEach { screen ->
            main.clear()
            main.appendChild(screen)
        }.launchIn(viewModel.scope)
    }

    // the shader is reading theme from window property, so we need a generic logic setting it up
    viewModel.navigationViewModel.themeLabel.onEach { theme ->
        val body = root.unsafeCast<HTMLElement>()
        when (theme) {
            LIGHT -> {
                body -= "dark"
                body += "light"
            }
            DARK -> {
                body -= "light"
                body += "dark"
            }
        }
        window.asDynamic().theme = theme.name.lowercase()
    }.launchIn(viewModel.scope)

}
