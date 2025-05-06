/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.kotlin

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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