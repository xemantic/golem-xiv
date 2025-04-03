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
import com.xemantic.ai.golem.presenter.context.ContextPresenter
import com.xemantic.ai.golem.presenter.context.ContextView
import com.xemantic.ai.golem.presenter.websocket.sendToGolem
import com.xemantic.ai.golem.presenter.websocket.collectGolemOutput
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

interface MainView {

    fun contextView(): ContextView

    fun displayContext(view: ContextView)

}

class MainPresenter(
    private val config: Config
) {

    data class Config(
        val apiProtocol: URLProtocol,
        val apiHost: String,
        val apiPort: Int,
        val wsProtocol: URLProtocol = if (apiProtocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
    )

    private val logger = KotlinLogging.logger {}

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

    var contextPresenter: ContextPresenter? = null

//    val golemService: GolemService
    init {



    }

    fun bind(
        view: MainView
    ) {

        scope.launch {
            val x = apiClient.get("/ping").bodyAsText()
            logger.error { "$ ---- ${x}" }
        }
        val contextView = view.contextView()

        view.displayContext(contextView)

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
                val sender = ::sendToGolem
                contextPresenter = ContextPresenter(
                    scope,
                    contextView,
                    sender
                )
                collectGolemOutput { handle(it) }
            }
        }

        scope.launch {
//            val contexts = golemService.listContexts()
        }

    }

    fun showContext(uuid: Uuid) {

    }

    fun onContextSelected() {

    }

    private suspend fun WebSocketSession.handle(
        output: GolemOutput
    ) {
        logger.info { this }
        when (output) {
            is GolemOutput.Welcome -> {
                if (contextPresenter != null) {
                    contextPresenter!!.start()
                }
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
