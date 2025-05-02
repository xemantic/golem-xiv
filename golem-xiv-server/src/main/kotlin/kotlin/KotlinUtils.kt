/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.ai.golem.server.kotlin

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun String.startsWithAnyOf(
    vararg strings: String
): Boolean = strings.any {
    startsWith(it)
}

// Might be needed for other agents, so we can move it to a library
fun describeCurrentMoment(): String {
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    val dateTime = now.toLocalDateTime(timeZone)
    val dayOfWeek = dateTime.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val month = dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "The current date is $dayOfWeek, $month ${dateTime.dayOfMonth}, ${dateTime.year} " +
            "at ${dateTime.asOnClock} ${timeZone.id}"
}

private val LocalDateTime.asOnClock
    get() =
        "${this.hour.time}:${this.minute.time}:${this.second.time}"

private val Int.time get() = toString().padStart(2, '0')
