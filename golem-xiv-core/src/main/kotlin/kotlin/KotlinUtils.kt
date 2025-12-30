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

package com.xemantic.ai.golem.core.kotlin

import com.xemantic.ai.golem.core.GolemXiv
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun String.startsWithAnyOf(
    vararg strings: String
): Boolean = strings.any {
    startsWith(it)
}

// Might be needed for other agents, so we can move it to a library
fun describeCurrentMoment(): String {
    val now = ZonedDateTime.now()
    val dayOfWeek = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val readable = now.format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm:ss zzz", Locale.ENGLISH))
    val iso = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    return "The current date and time is: $iso ($dayOfWeek, $readable)."
}

suspend fun <T> Collection<Deferred<T>>.awaitEach(
    onEach: suspend (T) -> Unit
): List<T> = coroutineScope {
    if (isEmpty()) return@coroutineScope emptyList()

    // Map each deferred to a new deferred that processes the result
    val processed = mapIndexed { index, deferred ->
        async {
            val result = deferred.await()
            onEach(result)
            index to result
        }
    }

    // Await all the processed deferreds and sort the results by index
    processed.awaitAll().sortedBy { it.first }.map { it.second }
}


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun getClasspathResource(
    name: String
) = GolemXiv::class.java.getResource(name).readText()
