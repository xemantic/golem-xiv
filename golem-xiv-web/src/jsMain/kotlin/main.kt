/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
import com.xemantic.ai.golem.web.navigation.HtmlSidebarView
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import io.ktor.http.URLProtocol
import kotlinx.browser.document
import kotlinx.browser.window
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
    val port = if (window.location.port.isEmpty()) 80 else window.location.port.toInt()
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

    val sidebarView = HtmlSidebarView()
    val headerView = HtmlHeaderView()
    val view = HtmlMainView(
        document.body!!,
        headerView,
        sidebarView
    )

    val localStorage = BrowserLocalStorage()
    val themeManager = LocalStorageThemeManager(
        localStorage = localStorage,
        defaultTheme = BrowserDefaultThemeProvider().defaultTheme
    )


    MainPresenter(
        scope = scope,
        config,
        view,
        headerView,
        sidebarView,
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
