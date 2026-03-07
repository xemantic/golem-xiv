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

import com.xemantic.kotlin.test.assert
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test

class LocalStorageThemeManagerTest {

    @Test
    fun `should return theme from local storage`() {
        // given
        val localStorage = mock<LocalStorage>()
        every { localStorage[any()] } returns "DARK"
        val manager = LocalStorageThemeManager(localStorage, defaultTheme = Theme.LIGHT)

        // when
        val theme = manager.theme

        // then
        assert(theme == Theme.DARK)
    }

    @Test
    fun `should return default theme when local storage has no theme`() {
        // given
        val localStorage = mock<LocalStorage>()
        every { localStorage[any()] } returns null
        val manager = LocalStorageThemeManager(localStorage, defaultTheme = Theme.LIGHT)

        // when
        val theme = manager.theme

        // then
        assert(theme == Theme.LIGHT)
    }

    @Test
    fun `should return default theme when local storage has invalid theme`() {
        // given
        val localStorage = mock<LocalStorage>()
        every { localStorage[any()] } returns "INVALID"
        val manager = LocalStorageThemeManager(localStorage, defaultTheme = Theme.DARK)

        // when
        val theme = manager.theme

        // then
        assert(theme == Theme.DARK)
    }

    @Test
    fun `should store theme in local storage`() {
        // given
        val localStorage = mock<LocalStorage>()
        every { localStorage[any()] } returns null
        every { localStorage[any()] = any() } returns Unit
        val manager = LocalStorageThemeManager(localStorage, defaultTheme = Theme.LIGHT)

        // when
        manager.theme = Theme.DARK

        // then
        verify {
            localStorage["theme"] = "DARK"
        }
    }

}