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

import com.xemantic.golem.viewmodel.environment.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val navigation: Navigation,
    resizes: Flow<Unit>,
    private val themeManager: ThemeManager,
    val scope: CoroutineScope // shares cope with the AppViewModel
) {

    init {
        resizes.onEach { closeSideMenu() }.launchIn(scope)
    }

    val sideMenuOpened: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val railOpened: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val cognitionBadge: StateFlow<Int>
        field = MutableStateFlow(1)

    val theme = MutableStateFlow(themeManager.theme)

    val activeTarget: StateFlow<Navigation.Target> = navigation.activeTarget

    init {
        theme.onEach { theme ->
            themeManager.theme = theme
        }.launchIn(scope)
    }

    fun onCognitions() {
        goTo(Navigation.Target.Cognition())
    }

    fun onWorkspace(path: String? = null) {
        goTo(Navigation.Target.Workspace(path))
    }

    fun onMemory() {
        goTo(Navigation.Target.Memory)
    }

    fun onSolicitations() {
        goTo(Navigation.Target.Solicitations)
    }

    fun onExchanges() {
        goTo(Navigation.Target.Exchanges)
    }

    fun onComputers(id: Long? = null) {
        goTo(Navigation.Target.Computers(id))
    }

    fun onSettings() {
        goTo(Navigation.Target.Settings)
    }

    fun onThemeToggle() {
        theme.value = theme.value.opposite()
    }

    fun onSideMenuToggle() {
        sideMenuOpened.value = !sideMenuOpened.value
    }

    fun onRailToggle() {
        railOpened.value = !railOpened.value
    }

    fun closeSideMenu() {
        sideMenuOpened.value = false
    }

    private fun goTo(target: Navigation.Target) {
        sideMenuOpened.value = false
        scope.launch {
            navigation.navigateTo(target)
        }
    }

}
