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

import com.xemantic.kotlin.test.assert
import kotlin.test.Test

class NavigationTargetTest {

    @Test
    fun `Cognitions target should have serial name cognition`() {
        assert(Navigation.Target.Cognition().name == "cognition")
    }

    @Test
    fun `Workspace target should have serial name workspace`() {
        assert(Navigation.Target.Workspace().name == "workspace")
    }

    @Test
    fun `Memory target should have serial name memory`() {
        assert(Navigation.Target.Memory.name == "memory")
    }

    @Test
    fun `Solicitations target should have serial name solicitations`() {
        assert(Navigation.Target.Solicitations.name == "solicitations")
    }

    @Test
    fun `Computers target should have serial name computers`() {
        assert(Navigation.Target.Computers().name == "computers")
    }

    @Test
    fun `Settings target should have serial name settings`() {
        assert(Navigation.Target.Settings.name == "settings")
    }

    @Test
    fun `NotFound target should have serial name notFound`() {
        assert(
            Navigation.Target.NotFound(
                message = "",
                pathname = ""
            ).name == "notFound"
        )
    }

}