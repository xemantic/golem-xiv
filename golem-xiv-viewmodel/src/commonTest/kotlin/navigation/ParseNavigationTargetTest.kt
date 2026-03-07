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

import com.xemantic.kotlin.test.be
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlin.test.Test

class ParseNavigationTargetTest {

    @Test
    fun `should parse empty path as cognitions`() {
        Navigation.Target.parse("") should {
            be<Navigation.Target.Cognitions>()
        }
    }

    @Test
    fun `should parse root target as unspecified cognition list`() {
        Navigation.Target.parse("/") should {
            be<Navigation.Target.Cognitions>()
        }
    }

    @Test
    fun `should parse cognition target`() {
        Navigation.Target.parse("/cognitions/42") should {
            be<Navigation.Target.Cognition>()
            have(id == 42L)
        }
    }

    @Test
    fun `should parse memory target`() {
        Navigation.Target.parse("/memory") should {
            be<Navigation.Target.Memory>()
        }
    }

    @Test
    fun `should parse settings target`() {
        Navigation.Target.parse("/settings") should {
            be<Navigation.Target.Settings>()
        }
    }

    @Test
    fun `should parse invalid cognition id as not found`() {
        Navigation.Target.parse("/cognitions/abc") should {
            be<Navigation.Target.NotFound>()
            have(message == "Invalid cognition id (must be a number): abc")
            have(pathname == "/cognitions/abc")
        }
    }

    @Test
    fun `should parse unknown path as not found`() {
        Navigation.Target.parse("/unknown/path") should {
            be<Navigation.Target.NotFound>()
            have(message == "No such path: /unknown/path")
            have(pathname == "/unknown/path")
        }
    }

}
