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
import com.xemantic.kotlin.test.assert
import dev.mokkery.*
import dev.mokkery.answering.returns
import dev.mokkery.matcher.any
import dev.mokkery.verify.VerifyMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class NavigationViewModelTest {

    @Test
    fun `should initialize themeLabel according to the current theme`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // then
        assert(viewModel.theme.value == Theme.LIGHT)
        verify(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should change theme and update themeLabel`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // when
        viewModel.theme.value = Theme.DARK

        // then
        verify(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
            themeManager.theme = Theme.DARK
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should navigate to Cognitions`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Settings)
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // when
        viewModel.onCognitions()

        // then
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
            navigation.navigateTo(Navigation.Target.Cognition())
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should navigate to Workspace`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // when
        viewModel.onWorkspace()

        // then
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
            navigation.navigateTo(Navigation.Target.Workspace())
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should navigate to Memory`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // when
        viewModel.onMemory()

        // then
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
            navigation.navigateTo(Navigation.Target.Memory)
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should navigate to Solicitations`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // when
        viewModel.onSolicitations()

        // then
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
            navigation.navigateTo(Navigation.Target.Solicitations)
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should navigate to Computers`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // when
        viewModel.onComputers()

        // then
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
            navigation.navigateTo(Navigation.Target.Computers())
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should navigate to Settings`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // when
        viewModel.onSettings()

        // then
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
            navigation.navigateTo(Navigation.Target.Settings)
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should initialize with menu closed`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // then
        assert(!viewModel.sideMenuOpened.value)
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should open menu on toggle`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        // when
        viewModel.onSideMenuToggle()

        // then
        assert(viewModel.sideMenuOpened.value)
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should close menu on second toggle`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        viewModel.onSideMenuToggle()

        // when
        viewModel.onSideMenuToggle()

        // then
        assert(!viewModel.sideMenuOpened.value)
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should close menu explicitly`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = emptyFlow<Unit>()
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        viewModel.onSideMenuToggle() // open first

        // when
        viewModel.closeSideMenu()

        // then
        assert(!viewModel.sideMenuOpened.value)
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should close menu on resize`() = runTest {
        // given
        val navigation = mock<Navigation> {
            every { activeTarget } returns MutableStateFlow(Navigation.Target.Cognition())
            everySuspend { navigateTo(any()) } returns Unit
        }
        val themeManager = mock<ThemeManager> {
            every { theme = any() } returns Unit
            every { theme } returns Theme.LIGHT
        }
        val resizes = MutableSharedFlow<Unit>()

        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        viewModel.onSideMenuToggle() // open first

        // when
        resizes.emit(Unit)

        // then
        assert(!viewModel.sideMenuOpened.value)
        verifySuspend(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.activeTarget
            themeManager.theme = Theme.LIGHT
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

}
