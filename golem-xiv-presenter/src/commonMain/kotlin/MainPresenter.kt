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

package com.xemantic.ai.golem.presenter

import com.xemantic.ai.golem.api.GolemInput
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.service.ClientContextService
import com.xemantic.ai.golem.api.service.ClientPingService
import com.xemantic.ai.golem.presenter.context.ContextPresenter
import com.xemantic.ai.golem.presenter.context.ContextView
import com.xemantic.ai.golem.presenter.navigation.HeaderPresenter
import com.xemantic.ai.golem.presenter.navigation.HeaderView
import com.xemantic.ai.golem.presenter.navigation.SidebarPresenter
import com.xemantic.ai.golem.presenter.navigation.SidebarView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.presenter.websocket.sendToGolem
import com.xemantic.ai.golem.presenter.websocket.collectGolemOutput
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLProtocol
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

interface MainView {

    fun theme(theme: Theme)

    fun contextView(): ContextView

    fun displayContext(view: ContextView)

    val resizes: Flow<Action>

    val contextSelection: Flow<Uuid>

}

class MainPresenter(
    private val config: Config,
    private val view: MainView,
    headerView: HeaderView,
    private val sidebarView: SidebarView,
) {

    private val logger = KotlinLogging.logger {}

    data class Config(
        val apiProtocol: URLProtocol,
        val apiHost: String,
        val apiPort: Int,
        val wsProtocol: URLProtocol = if (apiProtocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
    )

    private val scope = MainScope()

    private val apiClient = HttpClient {
        install(WebSockets)
        defaultRequest {
            url {
                protocol = config.apiProtocol
                host = config.apiHost
                port = config.apiPort
            }
        }
    }

    private val toggleFlow = MutableSharedFlow<Action>()

    val headerPresenter = HeaderPresenter(
        scope,
        headerView,
        menuToggleHandler = { toggleFlow.emit(Action) }
    )

    val sidebarPresenter = SidebarPresenter(
        scope,
        sidebarView,
        toggles = toggleFlow
    )

    private val golemInput = MutableSharedFlow<GolemInput>()

    private val pingService = ClientPingService(apiClient)
    private val contextService = ClientContextService(apiClient)

    private lateinit var contextPresenter: ContextPresenter
    private lateinit var contextView: ContextView

    init {

        scope.launch {
            sidebarView.themeChanges.collect {
                view.theme(it)
            }
        }

        scope.launch {
            val pong = pingService.ping()
            logger.info { "Server ping: $pong" }
        }

        scope.launch {
            view.contextSelection.collect {
                initContex()
                contextPresenter.loadContext(it)
                contextPresenter.dispose()
                //contextView
            }
        }

        scope.launch {
            logger.error { "wsPort ${config.apiPort}" }
            apiClient.webSocket(
                request = {
                    url.host = config.apiHost
                    url.port = config.apiPort
                    url.protocol = config.wsProtocol
                },
                path = "/ws"
            ) {
                launch {
                    golemInput.collect { sendToGolem(it) }
                }
                collectGolemOutput { handle(it) }
            }
        }

        initContex()
    }

    fun initContex() {
        if (::contextPresenter.isInitialized) {
            contextPresenter.dispose()
        }
        contextView = view.contextView()
        contextPresenter = ContextPresenter(
            scope,
            Dispatchers.Default,
            contextService,
            contextView,
            golemInput
        )
        view.displayContext(contextView)
    }

    fun onContextSelected() {

    }

    private suspend fun WebSocketSession.handle(
        output: GolemOutput
    ) {
        logger.info { this }
        when (output) {
            is GolemOutput.Welcome -> {
                sendToGolem(GolemInput.Test("foo"))
            }
            is GolemOutput.OsProcess -> {
                if (true) { // process in current context or child context
                    // update context view
                }
            }
            else -> {

            }
        }
    }

}
