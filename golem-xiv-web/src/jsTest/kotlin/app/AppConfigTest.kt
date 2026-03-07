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

package com.xemantic.golem.web.app

import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AppConfigTest {

    @Test
    fun `should validate config with lowercase log level`() {
        // given
        val appConfig = js("{logLevel: \"debug\"}").unsafeCast<AppConfig>()

        // when
        appConfig.validate()

        // then is valid
    }

    @Test
    fun `should validate config with uppercase log level`() {
        // given
        val appConfig = js("{logLevel: \"DEBUG\"}").unsafeCast<AppConfig>()

        // when
        appConfig.validate()

        // then is valid
    }

    @Test
    fun `should validate config with mixed case log level`() {
        // given
        val appConfig = js("{logLevel: \"Info\"}").unsafeCast<AppConfig>()

        // when
        appConfig.validate()

        // then is valid
    }

    @Test
    fun `should fail validation for invalid log level`() {
        assertFailsWith<IllegalArgumentException> {
            js("{logLevel: \"invalid\"}").unsafeCast<AppConfig>().validate()
        } should {
            have(message == "logLevel: must be one of: [TRACE, DEBUG, INFO, WARN, ERROR, OFF]")
        }
    }

    @Test
    fun `should fail validation for empty log level`() {
        assertFailsWith<IllegalArgumentException> {
            js("{logLevel: \"\"}").unsafeCast<AppConfig>().validate()
        } should {
            have(message == "logLevel: must be one of: [TRACE, DEBUG, INFO, WARN, ERROR, OFF]")
        }
    }

    @Test
    fun `should return default config when window CONFIG is not set`() {
        // given
        js("delete window.__CONFIG__")

        // when
        val config = appConfig()

        // then
        config should {
            have(logLevel == "debug")
            have(!devMode)
        }
    }

    @Test
    fun `should return window CONFIG when it is set`() {
        // given
        js("window.__CONFIG__ = {logLevel: \"info\", devMode: true}")

        try {
            // when
            val config = appConfig()

            // then
            config should {
                have(logLevel == "info")
                have(devMode)
            }
        } finally {
            js("delete window.__CONFIG__")
        }
    }

    @Test
    fun `should not validate window CONFIG`() {
        // given
        js("window.__CONFIG__ = {logLevel: \"invalid\", devMode: false}")

        try {
            // when
            val config = appConfig()

            // then - no validation exception, raw config returned as-is
            config should {
                have(logLevel == "invalid")
            }
        } finally {
            js("delete window.__CONFIG__")
        }
    }

}
