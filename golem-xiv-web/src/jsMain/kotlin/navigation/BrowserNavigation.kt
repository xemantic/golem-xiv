/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
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

package com.xemantic.golem.web.navigation

import com.xemantic.ai.golem.api.golemJson
import com.xemantic.golem.viewmodel.navigation.Navigation
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class BrowserNavigation : Navigation {

    private val logger = KotlinLogging.logger {}

    private val _targets = MutableSharedFlow<Navigation.Target>()

    override val targets: Flow<Navigation.Target> = _targets

    override suspend fun navigateTo(
        target: Navigation.Target
    ) {

        logger.debug { "Navigating to: $target" }

        val data = golemJson.encodeToString(target)

        when (target) {

            is Navigation.Target.Cognitions -> {
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
                    url = "/cognition/${target.id}"
                )
            }

            is Navigation.Target.Memory -> {
                window.history.pushState(
                    data = data,
                    title = "Memory",
                    url = "/memory"
                )
            }

            is Navigation.Target.Solicitations -> {
                window.history.pushState(
                    data = data,
                    title = "Solicitations",
                    url = "/solicitations"
                )
            }

            is Navigation.Target.Settings -> {
                window.history.pushState(
                    data = data,
                    title = "Settings",
                    url = "/settings"
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

        _targets.emit(target)

    }

}
