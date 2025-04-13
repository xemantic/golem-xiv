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
