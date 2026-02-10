/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
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

package com.xemantic.ai.golem.ddgs

import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

@EnabledIfSystemProperty(named = "ddgs.tests.enabled", matches = "true")
class DdgsClientTest {

    companion object {
        @JvmStatic
        @AfterAll
        fun tearDown() {
            TestDdgs.stop()
        }
    }

    @Test
    fun `should check health status`() = runTest {
        // given
        val client = TestDdgs.client

        // when
        val health = client.checkHealth()

        // then
        health should {
            have(status == "ok")
        }
    }

    @Test
    fun `should search text`() = runTest {
        // given
        val client = TestDdgs.client

        // when
        val results = client.searchText(
            query = "Kotlin programming language",
            maxResults = 5
        )

        // then
        assert(results.isNotEmpty())
        assert(results.all { it.title.isNotBlank() })
        assert(results.all { it.href.isNotBlank() })
        assert(results.all { it.body.isNotBlank() })
    }

    @Test
    fun `should search images`() = runTest {
        // given
        val client = TestDdgs.client

        // when
        val results = client.searchImages(
            query = "kotlin logo",
            maxResults = 3
        )

        // then
        assert(results.isNotEmpty())
        assert(results.all { it.title.isNotBlank() })
        assert(results.all { it.image.isNotBlank() })
        assert(results.all { it.url.isNotBlank() })
    }

    @Test
    fun `should search news`() = runTest {
        // given
        val client = TestDdgs.client

        // when
        val results = client.searchNews(
            query = "technology",
            maxResults = 5
        )

        // then
        assert(results.isNotEmpty())
        assert(results.all { it.title.isNotBlank() })
        assert(results.all { it.url.isNotBlank() })
        assert(results.all { it.body.isNotBlank() })
    }

    @Test
    fun `should search videos`() = runTest {
        // given
        val client = TestDdgs.client

        // when
        val results = client.searchVideos(
            query = "kotlin tutorial",
            maxResults = 3
        )

        // then
        assert(results.isNotEmpty())
        assert(results.all { it.title.isNotBlank() })
        assert(results.all { it.embedUrl.isNotBlank() })
    }

    @Test
    fun `should search books`() = runTest {
        // given
        val client = TestDdgs.client

        // when
        val results = client.searchBooks(
            query = "programming",
            maxResults = 5
        )

        // then
        assert(results.isNotEmpty())
        assert(results.all { it.title.isNotBlank() })
        assert(results.all { it.url.isNotBlank() })
    }
}
