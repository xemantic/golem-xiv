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
import com.xemantic.kotlin.test.sameAsJson
import com.xemantic.kotlin.test.should
import kotlin.test.Test

class NavigationTargetSerializationTest {

    @Test
    fun `should serialize Cognitions without id`() {
        // given
        val target = Navigation.Target.Cognition()

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "cognition"
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize Cognitions without id`() {
        // given
        val json = /* language=json */ """
            {
              "type": "cognition"
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.Cognition>()
            have(cogitationId == null)
        }
    }

    @Test
    fun `should serialize Cognitions with id`() {
        // given
        val target = Navigation.Target.Cognition(cogitationId = 42L)

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "cognition",
              "cogitationId": 42
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize Cognitions with id`() {
        // given
        val json = /* language=json */ """
            {
              "type": "cognition",
              "cogitationId": 42
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.Cognition>()
            have(cogitationId == 42L)
        }
    }

    @Test
    fun `should serialize Workspace without path`() {
        // given
        val target = Navigation.Target.Workspace()

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "workspace"
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize Workspace without path`() {
        // given
        val json = /* language=json */ """
            {
              "type": "workspace"
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.Workspace>()
            have(path == null)
        }
    }

    @Test
    fun `should serialize Workspace with path`() {
        // given
        val target = Navigation.Target.Workspace(path = "/foo/bar")

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "workspace",
              "path": "/foo/bar"
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize Workspace with path`() {
        // given
        val json = /* language=json */ """
            {
              "type": "workspace",
              "path": "/foo/bar"
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.Workspace>()
            have(path == "/foo/bar")
        }
    }

    @Test
    fun `should serialize Memory`() {
        // given
        val target = Navigation.Target.Memory

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "memory"
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize Memory`() {
        // given
        val json = /* language=json */ """
            {
              "type": "memory"
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.Memory>()
        }
    }

    @Test
    fun `should serialize Solicitations`() {
        // given
        val target = Navigation.Target.Solicitations

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "solicitations"
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize Solicitations`() {
        // given
        val json = /* language=json */ """
            {
              "type": "solicitations"
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.Solicitations>()
        }
    }

    @Test
    fun `should serialize Computers without id`() {
        // given
        val target = Navigation.Target.Computers()

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "computers"
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize Computers without id`() {
        // given
        val json = /* language=json */ """
            {
              "type": "computers"
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.Computers>()
            have(id == null)
        }
    }

    @Test
    fun `should serialize Computers with id`() {
        // given
        val target = Navigation.Target.Computers(id = 7L)

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "computers",
              "id": 7
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize Computers with id`() {
        // given
        val json = /* language=json */ """
            {
              "type": "computers",
              "id": 7
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.Computers>()
            have(id == 7L)
        }
    }

    @Test
    fun `should serialize Settings`() {
        // given
        val target = Navigation.Target.Settings

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "settings"
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize Settings`() {
        // given
        val json = /* language=json */ """
            {
              "type": "settings"
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.Settings>()
        }
    }

    @Test
    fun `should serialize NotFound`() {
        // given
        val target = Navigation.Target.NotFound(
            message = "No such path: /bogus",
            pathname = "/bogus"
        )

        // when
        val json = target.toJson()

        // then
        json sameAsJson """
            {
              "type": "notFound",
              "message": "No such path: /bogus",
              "pathname": "/bogus"
            }
        """.trimIndent()
    }

    @Test
    fun `should deserialize NotFound`() {
        // given
        val json = /* language=json */ """
            {
              "type": "notFound",
              "message": "No such path: /bogus",
              "pathname": "/bogus"
            }
        """

        // when
        val target = Navigation.Target.fromJson(json)

        // then
        target should {
            be<Navigation.Target.NotFound>()
            have(message == "No such path: /bogus")
            have(pathname == "/bogus")
        }
    }

}