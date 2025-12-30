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

package com.xemantic.ai.golem.web

import com.xemantic.ai.golem.presenter.MainPresenter
import com.xemantic.ai.golem.presenter.environment.LocalStorageThemeManager
import com.xemantic.ai.golem.presenter.navigation.Navigation
import com.xemantic.ai.golem.presenter.navigation.parseNavigationTarget
import com.xemantic.ai.golem.web.dev.devMode
import com.xemantic.ai.golem.web.environment.BrowserDefaultThemeProvider
import com.xemantic.ai.golem.web.environment.BrowserLocalStorage
import com.xemantic.ai.golem.web.main.HtmlMainView
import com.xemantic.ai.golem.web.memory.HtmlMemoryView
import com.xemantic.ai.golem.web.navigation.HtmlHeaderView
import com.xemantic.ai.golem.web.navigation.HtmlNavigation
import com.xemantic.ai.golem.web.navigation.HtmlNotFoundView
import com.xemantic.ai.golem.web.navigation.HtmlNavigationRailView
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import io.ktor.http.URLProtocol
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

fun main() {

    KotlinLoggingConfiguration.logLevel = Level.TRACE // TODO should be configurable on production

    val scope = MainScope()

    val currentProtocol = window.location.protocol.substringBefore(":")
    val protocol = URLProtocol.byName[currentProtocol]
    requireNotNull(protocol) { "protocol cannot be null" }
    val host = window.location.hostname
    val port = if (window.location.port.isEmpty()) {
        if (currentProtocol == "http") 80 else 443
    } else {
        window.location.port.toInt()
    }
    val config =  MainPresenter.Config(
        apiHost = host,
        apiPort = if (devMode) 8081 else port,
        apiProtocol = protocol
    )

    val navigationTargets = MutableSharedFlow<Navigation.Target>()
    val navigation = HtmlNavigation(
        scope = scope,
        navigationTargetSink = navigationTargets
    )

    val navigationRailView = HtmlNavigationRailView()
    val headerView = HtmlHeaderView()
    val view = HtmlMainView(
        document.body!!,
        headerView,
        navigationRailView
    )

    val localStorage = BrowserLocalStorage()
    val themeManager = LocalStorageThemeManager(
        localStorage = localStorage,
        defaultTheme = BrowserDefaultThemeProvider().defaultTheme
    )

    MainPresenter(
        scope = scope,
        ioDispatcher = Dispatchers.Default,
        config = config,
        view,
        headerView,
        navigationRailView,
        navigation,
        navigationTargets = navigationTargets,
        memoryViewProvider = { HtmlMemoryView() },
        notFoundViewProvider = { HtmlNotFoundView() },
        themeManager = themeManager
    )

    val target = parseNavigationTarget(window.location.pathname)

    // it's important to lauch it in coroutine, so all the other coroutine and listeners can get registered
    scope.launch {
        navigationTargets.emit(target)
    }

}
