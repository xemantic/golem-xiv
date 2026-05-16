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

package com.xemantic.golem.dom.export

import com.xemantic.kotlin.js.JsObject
import com.xemantic.kotlin.js.isNullOrEmpty
import kotlinx.js.JsPlainObject

// Check out: https://github.com/xemantic/markanywhere
// The markanywhere library defines semantic events, and here we are only repeating JS-side contract

@Suppress("unused")
@JsPlainObject
sealed external interface Event {
    val type: String
}

@Suppress("unused")
@JsPlainObject
external interface MarkEvent : Event {
    val name: String
    val tagged: Boolean
    val attributes: JsObject?
}

@Suppress("unused")
@JsPlainObject
external interface UnmarkEvent : Event {
    val tagged: Boolean
    val name: String
}

@Suppress("unused")
@JsPlainObject
external interface TextEvent : Event {
    val text: String
}

@Suppress("NOTHING_TO_INLINE")
inline fun MarkEvent(
    name: String,
    isTagged: Boolean = true,
    attributes: JsObject? = null
) = if (attributes.isNullOrEmpty()) {
    MarkEvent(
        type = "mark",
        name = name,
        tagged = isTagged
    )
} else {
    MarkEvent(
        type = "mark",
        name = name,
        tagged = isTagged,
        attributes = attributes
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun UnmarkEvent(
    name: String,
    isTagged: Boolean = true
) = UnmarkEvent(
    type = "unmark",
    name = name,
    tagged = isTagged
)

@Suppress("NOTHING_TO_INLINE")
inline fun TextEvent(
    text: String
) = TextEvent(
    type = "text",
    text = text
)
