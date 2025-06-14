/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter

import com.xemantic.ai.golem.api.GolemError
import com.xemantic.ai.golem.api.GolemInput
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.client.GolemServiceException
import com.xemantic.ai.golem.api.client.http.HttpClientCognitionService
import com.xemantic.ai.golem.api.client.http.HttpClientPingService
import com.xemantic.ai.golem.api.client.http.sendGolemData
import com.xemantic.ai.golem.presenter.environment.Theme
import com.xemantic.ai.golem.presenter.environment.ThemeManager
import com.xemantic.ai.golem.presenter.memory.MemoryView
import com.xemantic.ai.golem.presenter.navigation.HeaderPresenter
import com.xemantic.ai.golem.presenter.navigation.HeaderView
import com.xemantic.ai.golem.presenter.navigation.Navigation
import com.xemantic.ai.golem.presenter.navigation.NotFoundView
import com.xemantic.ai.golem.presenter.navigation.SidebarPresenter
import com.xemantic.ai.golem.presenter.navigation.SidebarView
import com.xemantic.ai.golem.presenter.cognition.CognitionPresenter
import com.xemantic.ai.golem.presenter.cognition.CognitionView
import com.xemantic.ai.golem.presenter.util.Action
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

interface MainView {

    fun theme(theme: Theme)

    fun cognitionView(): CognitionView // TODO maybe factory should be outside?

    fun display(view: ScreenView)

    val resizes: Flow<Action>

}

interface ScreenView

class MainPresenter(
    private val scope: CoroutineScope,
    private val config: Config,
    private val view: MainView,
    headerView: HeaderView,
    private val sidebarView: SidebarView,
    navigation: Navigation,
    navigationTargets: Flow<Navigation.Target>,
    private val memoryViewProvider: () -> MemoryView,
    private val notFoundViewProvider: () -> NotFoundView,
    private val themeManager: ThemeManager
) {

    private val logger = KotlinLogging.logger {}

    data class Config(
        val apiProtocol: URLProtocol,
        val apiHost: String,
        val apiPort: Int,
        val wsProtocol: URLProtocol = if (apiProtocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
    )

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

    private val themeChangesFlow = MutableSharedFlow<Theme>()

    private val memoryView by lazy { memoryViewProvider() }

    private val notFoundView by lazy { notFoundViewProvider() }

    val headerPresenter = HeaderPresenter(
        scope,
        headerView,
        menuToggleHandler = { toggleFlow.emit(Action) }
    )

    val sidebarPresenter = SidebarPresenter(
        scope,
        sidebarView,
        toggles = toggleFlow,
        navigation,
        themeChangesSink = themeChangesFlow
    )

//    private val golemInput = MutableSharedFlow<GolemInput>()

    private val golemInputs = MutableSharedFlow<GolemInput>()
    private val golemOutputs = MutableSharedFlow<GolemOutput>()

    private val pingService = HttpClientPingService(apiClient)
    private val cognitionService = HttpClientCognitionService(apiClient)

    private lateinit var cognitionPresenter: CognitionPresenter
    private lateinit var cognitionView: CognitionView

    init {
        logger.debug {
            "Main presenter init start"
        }
        val theme = themeManager.theme
        view.theme(theme)
        sidebarPresenter.theme = theme
        navigationTargets.onEach { target ->
            logger.info { "Navigation target in main presenter: $target" }
            when (target) {
                is Navigation.Target.InitiateCognition -> {
                    initContex()
                }
                is Navigation.Target.Cognition -> {
                    initContex()
                    try {
                        cognitionService.emitCognition(id = target.id)
                    } catch (e: GolemServiceException) {
                        if (e.error is GolemError.NoSuchCognition) {
                            navigation.navigateTo(Navigation.Target.NotFound(
                                message = "Cognition not found",
                                pathname = "/cognitions/${target.id}"
                            ))
                        } else {
                            throw e
                        }
                    }
                }
                is Navigation.Target.Memory -> {
                    view.display(memoryView)
                }
                is Navigation.Target.NotFound -> {
                    notFoundView.message = target.message
                    view.display(notFoundView)
                }
            }
            sidebarView.opened = false
        }.launchIn(scope)

        themeChangesFlow.onEach { theme ->
            themeManager.theme = theme
            sidebarPresenter.theme = theme
            view.theme(theme)
        }.launchIn(scope)

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
            logger.debug { "Connecting to WebSocket, port: ${config.apiPort}" }
            apiClient.webSocket(
                request = {
                    url.host = config.apiHost
                    url.port = config.apiPort
                    url.protocol = config.wsProtocol
                },
                path = "/ws"
            ) {
                launch {
                    // nothing to send at the moment
                    golemInputs.collect { intput ->
                        sendGolemData(intput)
                    }
                }
                collectGolemOutput { handle(it) }
            }
        }

//        initContex()

        logger.debug {
            "Context initiated"
        }
    }

    fun initContex() {
        if (::cognitionPresenter.isInitialized) {
            cognitionPresenter.dispose()
        }
        cognitionView = view.cognitionView()
        cognitionPresenter = CognitionPresenter(
            scope,
            Dispatchers.Default,
            cognitionService,
            cognitionView,
            golemOutputs
        )
        view.display(cognitionView)
    }

    fun onContextSelected() {

    }

    @OptIn(ExperimentalTime::class)
    private suspend fun WebSocketSession.handle(
        output: GolemOutput
    ) {
        when (output) {
            is GolemOutput.Welcome -> {
                logger.info { output.message }
            }
            else -> {
                golemOutputs.emit(output)
            }
        }
    }

}
