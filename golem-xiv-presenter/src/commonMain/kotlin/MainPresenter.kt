/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter

import com.xemantic.ai.golem.api.GolemInput
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.service.ClientCognitiveWorkspaceService
import com.xemantic.ai.golem.api.service.ClientPingService
import com.xemantic.ai.golem.presenter.navigation.HeaderPresenter
import com.xemantic.ai.golem.presenter.navigation.HeaderView
import com.xemantic.ai.golem.presenter.navigation.SidebarPresenter
import com.xemantic.ai.golem.presenter.navigation.SidebarView
import com.xemantic.ai.golem.presenter.phenomena.WorkspacePresenter
import com.xemantic.ai.golem.presenter.phenomena.WorkspaceView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.presenter.websocket.sendToGolem
import com.xemantic.ai.golem.presenter.websocket.collectGolemOutput
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

interface MainView {

    fun theme(theme: Theme)

    fun workspaceView(): WorkspaceView

    fun displayWorkspace(view: WorkspaceView)

    val resizes: Flow<Action>

    val workspaceSelection: Flow<String>

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
        install(ContentNegotiation) {
            json()
        }
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

    private val golemOutputs = MutableSharedFlow<GolemOutput>()

    private val pingService = ClientPingService(apiClient)
    private val workspaceService = ClientCognitiveWorkspaceService(apiClient)

    private lateinit var workspacePresenter: WorkspacePresenter
    private lateinit var workspaceView: WorkspaceView

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

        // TODO is it needed for a single context?
//        scope.launch {
//            view.contextSelection.collect {
//                initContex()
//                contextPresenter.ini(it)
//                contextPresenter.dispose()
//                //contextView
//            }
//        }

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
        if (::workspacePresenter.isInitialized) {
            workspacePresenter.dispose()
        }
        workspaceView = view.workspaceView()
        workspacePresenter = WorkspacePresenter(
            scope,
            Dispatchers.Default,
            workspaceService,
            workspaceView,
            golemOutputs
        )
        view.displayWorkspace(workspaceView)
    }

    fun onContextSelected() {

    }

    private suspend fun WebSocketSession.handle(
        output: GolemOutput
    ) {
        when (output) {
            is GolemOutput.Welcome -> {
                logger.info { output.message }
            }
            else -> {
                logger.info { "received $output" }
                golemOutputs.emit(output)
            }
        }
    }

}
