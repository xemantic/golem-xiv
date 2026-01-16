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

package com.xemantic.ai.golem.presenter

import com.xemantic.ai.golem.api.GolemError
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.client.GolemServiceException
import com.xemantic.ai.golem.api.client.http.HttpClientCognitionService
import com.xemantic.ai.golem.api.client.http.HttpClientPingService
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
import com.xemantic.ai.golem.presenter.sse.collectGolemOutput
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
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
    ioDispatcher: CoroutineDispatcher,
    private val config: Config,
    private val view: MainView,
    headerView: HeaderView,
    private val sidebarView: SidebarView,
    private val navigation: Navigation,
    navigationTargets: Flow<Navigation.Target>,
    private val memoryViewProvider: () -> MemoryView,
    private val notFoundViewProvider: () -> NotFoundView,
    private val themeManager: ThemeManager
) {

    private val logger = KotlinLogging.logger {}

    data class Config(
        val apiProtocol: URLProtocol,
        val apiHost: String,
        val apiPort: Int
    )

    private val apiClient = HttpClient {
        install(SSE)
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

    private var currentNavigationTarget: Navigation.Target? = null

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
                    resetCognition()
                }
                is Navigation.Target.Cognition -> {
                    resetCognition(cognitionId = target.id)
                    // Only replay stored messages when navigating to an existing cognition,
                    // not when transitioning from InitiateCognition (first message already streaming)
                    if (currentNavigationTarget !is Navigation.Target.InitiateCognition) {
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
                }
                is Navigation.Target.Memory -> {
                    view.display(memoryView)
                }
                is Navigation.Target.NotFound -> {
                    notFoundView.message = target.message
                    view.display(notFoundView)
                }
            }
            currentNavigationTarget = target
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
            logger.debug { "Connecting to SSE, port: ${config.apiPort}" }
            apiClient.sse(
                host = config.apiHost,
                port = config.apiPort,
                path = "/events"
            ) {
                collectGolemOutput { handle(it) }
            }
        }

        logger.debug {
            "Context initiated"
        }
    }

    fun resetCognition(cognitionId: Long? = null) {
        if (::cognitionPresenter.isInitialized) {
            cognitionPresenter.dispose()
        }
        cognitionView = view.cognitionView()
        cognitionPresenter = CognitionPresenter(
            cognitionId = cognitionId,
            view = cognitionView,
            mainScope = scope,
            ioDispatcher = Dispatchers.Default,
            cognitionService = cognitionService,
            golemOutputs = golemOutputs,
            navigation = navigation
        )
        view.display(cognitionView)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun handle(
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
