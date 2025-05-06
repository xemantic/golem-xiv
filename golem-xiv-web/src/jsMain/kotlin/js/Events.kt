/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
