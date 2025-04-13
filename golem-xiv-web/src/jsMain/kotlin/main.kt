/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.ai.golem.web

import com.xemantic.ai.golem.presenter.MainPresenter
import com.xemantic.ai.golem.web.dev.devMode
import com.xemantic.ai.golem.web.main.HtmlMainView
import com.xemantic.ai.golem.web.navigation.HtmlHeaderView
import com.xemantic.ai.golem.web.navigation.HtmlSidebarView
import io.ktor.http.URLProtocol
import kotlinx.browser.document
import kotlinx.browser.window

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
    val sidebarView = HtmlSidebarView()
    val headerView = HtmlHeaderView()
    val presenter = MainPresenter(
        config,
        headerView,
        sidebarView
    )
    val view = HtmlMainView(
        document.body!!,
        headerView,
        sidebarView
    )
    presenter.bind(view)
}
