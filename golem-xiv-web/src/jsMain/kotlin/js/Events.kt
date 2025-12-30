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

package com.xemantic.ai.golem.web.js

import com.xemantic.ai.golem.presenter.util.Action
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.MouseEvent

inline fun <reified T : Event> EventTarget.eventFlow(
    type: String
): Flow<T> = callbackFlow {
    val listener = object : EventListener {
        override fun handleEvent(event: Event) {
            trySend(event as T)
        }
    }
    addEventListener(type, listener)
    awaitClose {
        removeEventListener(type, listener)
    }
}

fun EventTarget.actions(): Flow<Action> = clicks().map { Action }

fun EventTarget.clicks(): Flow<MouseEvent> = eventFlow("click")

fun Window.resizes(): Flow<Event> = eventFlow("resize")
