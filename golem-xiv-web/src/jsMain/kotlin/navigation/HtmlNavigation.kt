/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.api.golemJson
import com.xemantic.ai.golem.presenter.navigation.Navigation
import com.xemantic.ai.golem.web.js.eventFlow
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.PopStateEvent

class HtmlNavigation(
    scope: CoroutineScope,
    private val navigationTargetSink: FlowCollector<Navigation.Target>
) : Navigation {

    private val logger = KotlinLogging.logger {}

    init {
        window.eventFlow<PopStateEvent>("popstate").onEach { event ->
            event.preventDefault()
            val target = golemJson.decodeFromString<Navigation.Target>(event.state as String)
            navigationTargetSink.emit(target)
        }.launchIn(scope)
    }

    override suspend fun navigateTo(
        target: Navigation.Target
    ) {

        logger.info {
            "Navigating to: $target"
        }

        val data = golemJson.encodeToString(target)

        when (target) {
            is Navigation.Target.InitiateCognition -> {
                window.history.pushState(
                    data = data,
                    title = "Initiate cognition",
                    url = "/"
                )
            }
            is Navigation.Target.Cognition -> {
                window.history.pushState(
                    data = data,
                    title = "Cognition",
                    url = "/cognitions/${target.id}"
                )
            }
            is Navigation.Target.Memory -> {
                window.history.pushState(
                    data = data,
                    title = "Memory",
                    url = "/memory"
                )
            }
            is Navigation.Target.NotFound -> {
                window.history.replaceState(
                    data = data,
                    title = target.message,
                    url = target.pathname
                )
            }
        }

        if (target !is Navigation.Target.NotFound) {
            navigationTargetSink.emit(target)
        }
    }

}
