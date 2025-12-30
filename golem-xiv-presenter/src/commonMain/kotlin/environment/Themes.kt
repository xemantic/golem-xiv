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

package com.xemantic.ai.golem.presenter.environment

enum class Theme {

    LIGHT,
    DARK;

    fun opposite() = when (this) {
        LIGHT -> DARK
        DARK -> LIGHT
    }

}

interface ThemeManager {

    var theme: Theme

}

interface DefaultThemeProvider {

    val defaultTheme: Theme

}

class LocalStorageThemeManager(
    private val localStorage: LocalStorage,
    defaultTheme: Theme
) : ThemeManager {

    private var currentTheme = localStorage.getItem(
        key = "theme"
    )?.let {
        try {
            Theme.valueOf(it)
        } catch (e : IllegalArgumentException) {
            // TODO logger here
            null
        }
    } ?: defaultTheme

    override var theme: Theme
        get() = currentTheme
        set(value) {
            currentTheme = value
            localStorage.setItem("theme", value.name)
        }

}
