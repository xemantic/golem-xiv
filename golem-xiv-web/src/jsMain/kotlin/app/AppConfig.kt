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

import io.github.oshai.kotlinlogging.Level

external interface AppConfig {
    val logLevel: String
    val devMode: Boolean
}

fun appConfig(): AppConfig = js(
    "window.__CONFIG__"
) ?: object : AppConfig {
    override val logLevel = "debug"
    override val devMode = false
}.apply {
    validate()
}

fun AppConfig.validate() {
    val levels = Level.entries.map { it.name }
    require(logLevel.uppercase() in levels) {
        "logLevel: must be one of: $levels"
    }
}
