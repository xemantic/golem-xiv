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

package com.xemantic.golem.viewmodel.navigation

import com.xemantic.golem.viewmodel.environment.Theme
import com.xemantic.golem.viewmodel.environment.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

class NavigationViewModel(
    private val navigation: Navigation,
    resizes: Flow<Unit>,
    private val themeManager: ThemeManager,
    val scope: CoroutineScope // shares cope with the AppViewModel
) {

    init {
        resizes.onEach { closeMenu() }.launchIn(scope)
    }

    val opened: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val themeLabel: StateFlow<Theme>
        field = MutableStateFlow(
            theme.opposite()
        )

    var theme
        get() = themeManager.theme
        set(value) {
            themeManager.theme = value
            themeLabel.value = value.opposite()
        }

    fun onCognitions() {
        navigation.navigateTo(Navigation.Target.Cognitions)
    }

    fun onMemory() {
        navigation.navigateTo(Navigation.Target.Memory)
    }

    fun onSettings() {
        navigation.navigateTo(Navigation.Target.Settings)
    }

    fun onThemeToggle() {
        theme = theme.opposite()
    }

    fun onMenuToggle() {
        opened.value = !opened.value
    }

    fun closeMenu() {
        opened.value = false
    }

}
