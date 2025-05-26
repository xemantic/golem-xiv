/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web

import com.xemantic.ai.golem.presenter.MainPresenter
import com.xemantic.ai.golem.presenter.navigation.Navigation
import com.xemantic.ai.golem.web.dev.devMode
import com.xemantic.ai.golem.web.main.HtmlMainView
import com.xemantic.ai.golem.web.memory.HtmlMemoryView
import com.xemantic.ai.golem.web.navigation.HtmlHeaderView
import com.xemantic.ai.golem.web.navigation.HtmlNavigation
import com.xemantic.ai.golem.web.navigation.HtmlSidebarView
import io.ktor.http.URLProtocol
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableSharedFlow

fun main() {
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
    val navigation = HtmlNavigation(navigationTargets)
    val sidebarView = HtmlSidebarView()
    val headerView = HtmlHeaderView()
    val view = HtmlMainView(
        document.body!!,
        headerView,
        sidebarView
    )
    MainPresenter(
        config,
        view,
        headerView,
        sidebarView,
        navigation,
        navigationTargets,
        memoryViewProvider = { HtmlMemoryView() }
    )
}
