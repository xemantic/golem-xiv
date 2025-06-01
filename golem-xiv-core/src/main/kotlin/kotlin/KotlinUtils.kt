/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.kotlin

import com.xemantic.ai.golem.core.Golem
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
) = Golem::class.java.getResource(name).readText()
