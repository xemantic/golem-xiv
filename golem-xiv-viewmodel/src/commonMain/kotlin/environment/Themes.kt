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

package com.xemantic.golem.viewmodel.environment

import io.github.oshai.kotlinlogging.KotlinLogging

enum class Theme {

    LIGHT,
    DARK;

    fun opposite() = when (this) {
        LIGHT -> DARK
        DARK -> LIGHT
    }

}

val Theme.label get() = when (this) {
    LIGHT -> "Dark mode"
    DARK -> "Light mode"
}

interface ThemeManager {

    var theme: Theme

}

interface DefaultThemeProvider {

    val defaultTheme: Theme

}

class LocalStorageThemeManager(
    private val localStorage: LocalStorage,
    private val defaultTheme: Theme
) : ThemeManager {

    private val logger = KotlinLogging.logger {}

    override var theme: Theme
        get() = (localStorage["theme"]?.let {
            try {
                Theme.valueOf(it)
            } catch (e : IllegalArgumentException) {
                logger.error(e) {
                    "Invalid theme name in local storage: $it" +
                            ", should be one of: [${Theme.entries.joinToString(", ")}]" +
                            ", initializing as null"
                }
                null
            }
        } ?: defaultTheme).also {
            logger.debug { "Getting theme: $it" }
        }
        set(value) {
            logger.debug { "Setting theme: $value" }
            localStorage["theme"] = value.name
        }

}
