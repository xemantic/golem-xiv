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
import kotlin.test.Test

class ThemeTest {

    @Test
    fun `LIGHT opposite should be DARK`() {
        assert(Theme.LIGHT.opposite() == Theme.DARK)
    }

    @Test
    fun `DARK opposite should be LIGHT`() {
        assert(Theme.DARK.opposite() == Theme.LIGHT)
    }

    @Test
    fun `LIGHT label should be Dark mode`() {
        assert(Theme.LIGHT.label == "Dark mode")
    }

    @Test
    fun `DARK label should be Light mode`() {
        assert(Theme.DARK.label == "Light mode")
    }

}
