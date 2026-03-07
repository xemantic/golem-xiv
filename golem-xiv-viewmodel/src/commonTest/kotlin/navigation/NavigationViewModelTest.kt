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
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class NavigationViewModelTest {

    @Test
    fun `should initialize themeLabel as opposite of current theme`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { themeManager.theme } returns Theme.LIGHT
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )

        // then
        assert(viewModel.themeLabel.value == Theme.DARK)
        verify { themeManager.theme }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should initialize themeLabel as LIGHT when current theme is DARK`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { themeManager.theme } returns Theme.DARK
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )

        // then
        assert(viewModel.themeLabel.value == Theme.LIGHT)
        verify { themeManager.theme }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should change theme and update themeLabel`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { themeManager.theme } returns Theme.LIGHT
        every { themeManager.theme = any() } returns Unit
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )

        // when
        viewModel.theme = Theme.DARK

        // then
        verify(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            themeManager.theme = Theme.DARK
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should toggle theme from LIGHT to DARK`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { themeManager.theme } returns Theme.LIGHT
        every { themeManager.theme = any() } returns Unit
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )

        // when
        viewModel.onThemeToggle()

        // then
        assert(viewModel.themeLabel.value == Theme.LIGHT)
        verify(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            themeManager.theme
            themeManager.theme = Theme.DARK
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should navigate to Cognitions`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { navigation.navigateTo(any()) } returns Unit
        every { themeManager.theme } returns Theme.LIGHT
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )

        // when
        viewModel.onCognitions()

        // then
        verify(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.navigateTo(Navigation.Target.Cognitions)
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should navigate to Memory`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { navigation.navigateTo(any()) } returns Unit
        every { themeManager.theme } returns Theme.LIGHT
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )

        // when
        viewModel.onMemory()

        // then
        verify(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.navigateTo(Navigation.Target.Memory)
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should navigate to Settings`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { navigation.navigateTo(any()) } returns Unit
        every { themeManager.theme } returns Theme.LIGHT
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )

        // when
        viewModel.onSettings()

        // then
        verify(VerifyMode.exhaustiveOrder) {
            themeManager.theme
            navigation.navigateTo(Navigation.Target.Settings)
        }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should initialize with menu closed`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { themeManager.theme } returns Theme.LIGHT
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )

        // then
        assert(!viewModel.opened.value)
        verify { themeManager.theme }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should open menu on toggle`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { themeManager.theme } returns Theme.LIGHT
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )

        // when
        viewModel.onMenuToggle()

        // then
        assert(viewModel.opened.value)
        verify { themeManager.theme }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should close menu on second toggle`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { themeManager.theme } returns Theme.LIGHT
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )
        viewModel.onMenuToggle()

        // when
        viewModel.onMenuToggle()

        // then
        assert(!viewModel.opened.value)
        verify { themeManager.theme }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @Test
    fun `should close menu explicitly`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = emptyFlow<Unit>()
        every { themeManager.theme } returns Theme.LIGHT
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = this,
        )
        viewModel.onMenuToggle() // open it first

        // when
        viewModel.closeMenu()

        // then
        assert(!viewModel.opened.value)
        verify { themeManager.theme }
        verifyNoMoreCalls(navigation, themeManager)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should close menu on resize`() = runTest {
        // given
        val navigation = mock<Navigation>()
        val themeManager = mock<ThemeManager>()
        val resizes = MutableSharedFlow<Unit>()
        every { themeManager.theme } returns Theme.LIGHT
        val viewModel = NavigationViewModel(
            navigation,
            resizes,
            themeManager,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        viewModel.onMenuToggle() // open it first
        assert(viewModel.opened.value)

        // when
        resizes.emit(Unit)

        // then
        assert(!viewModel.opened.value)
        verify { themeManager.theme }
        verifyNoMoreCalls(navigation, themeManager)
    }

}
