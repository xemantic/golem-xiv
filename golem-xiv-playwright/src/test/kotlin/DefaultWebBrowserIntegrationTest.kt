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

package com.xemantic.ai.golem.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.xemantic.kotlin.test.assert
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import java.net.HttpURLConnection
import java.net.URL
import kotlin.test.Test

/**
 * Integration tests for DefaultWebBrowser that open real websites.
 *
 * These tests require:
 * - Network connectivity
 * - Playwright browser installed
 *
 * Run these tests with:
 * ```
 * # Run all integration tests
 * ./gradlew :golem-xiv-playwright:test --tests "*Integration*"
 *
 * # Or by tag
 * ./gradlew :golem-xiv-playwright:test --tests "*" -Dkotest.tags="integration"
 * ```
 *
 * Tests will be skipped (not failed) if network is not available.
 */
@Tag("integration")
class DefaultWebBrowserIntegrationTest {

    companion object {
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser
        private var networkAvailable = false

        @JvmStatic
        @BeforeAll
        fun setup() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(true)
            )

            // Check network availability
            networkAvailable = checkNetworkAvailability()
            println("Network is ${if (networkAvailable) "available" else "not available"}")
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            browser.close()
            playwright.close()
        }

        private fun checkNetworkAvailability(): Boolean {
            return try {
                val url = URL("https://example.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()
                val responseCode = connection.responseCode
                connection.disconnect()
                responseCode in 200..399
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun assumeNetworkAvailable() {
        assumeTrue(
            networkAvailable,
            "Network is not available for integration tests"
        )
    }

    @Test
    fun `should open simple static page`() = runTest {
        assumeNetworkAvailable()

        // given
        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("https://example.com")

        // then - smoke test: page opens and returns non-empty content
        assert(markdown.isNotEmpty())
        assert("example" in markdown.lowercase())
    }

    @Test
    fun `should open JavaScript-rendered page`() = runTest {
        assumeNetworkAvailable()

        // given
        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("https://github.com")

        // then - smoke test: JS-heavy page opens and returns non-empty content
        assert(markdown.isNotEmpty())
    }

}
