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
    fun `should parse empty path as Cognition`() {
        Navigation.Target.parse("") should {
            be<Navigation.Target.Cognition>()
            have(id == null)
        }
    }

    @Test
    fun `should parse root target as unspecified Cognition`() {
        Navigation.Target.parse("/") should {
            be<Navigation.Target.Cognition>()
            have(id == null)
        }
    }

    @Test
    fun `should parse Cognition target with id`() {
        Navigation.Target.parse("/cognition/42") should {
            be<Navigation.Target.Cognition>()
            have(id == 42L)
        }
    }

    @Test
    fun `should parse Workspace target`() {
        Navigation.Target.parse("/workspace") should {
            be<Navigation.Target.Workspace>()
            have(path == null)
        }
    }

    @Test
    fun `should parse Workspace target with path`() {
        Navigation.Target.parse("/workspace/foo/bar") should {
            be<Navigation.Target.Workspace>()
            have(path == "/foo/bar")
        }
    }

    @Test
    fun `should parse Memory target`() {
        Navigation.Target.parse("/memory") should {
            be<Navigation.Target.Memory>()
        }
    }

    @Test
    fun `should parse Solicitations target`() {
        Navigation.Target.parse("/solicitations") should {
            be<Navigation.Target.Solicitations>()
        }
    }

    @Test
    fun `should parse Computers target`() {
        Navigation.Target.parse("/computers") should {
            be<Navigation.Target.Computers>()
            have(id == null)
        }
    }

    @Test
    fun `should parse Settings target`() {
        Navigation.Target.parse("/settings") should {
            be<Navigation.Target.Settings>()
        }
    }

    @Test
    fun `should parse invalid cognition id as not found`() {
        Navigation.Target.parse("/cognition/abc") should {
            be<Navigation.Target.NotFound>()
            have(message == "Invalid cognition id (must be a number): abc")
            have(pathname == "/cognition/abc")
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
