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

package com.xemantic.ai.golem.json

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

sealed interface JsonEvent {

    object DocumentStart : JsonEvent { override fun toString(): String = "DocumentStart" }
    object DocumentEnd : JsonEvent { override fun toString(): String = "DocumentEnd" }

    object ObjectStart : JsonEvent { override fun toString(): String = "ObjectStart" }
    object ObjectEnd : JsonEvent { override fun toString(): String = "ObjectEnd" }

    @JvmInline
    value class PropertyName(val name: String) : JsonEvent { override fun toString(): String = "PropertyName(\"$name\")" }

    object ArrayStart : JsonEvent { override fun toString(): String = "ArrayStart" }
    object ArrayEnd : JsonEvent { override fun toString(): String = "ArrayEnd" }

    object StringStart : JsonEvent { override fun toString(): String = "StringStart" }
    @JvmInline
    value class StringDelta(val chunk: String) : JsonEvent { override fun toString(): String = "StringDelta($chunk)" }
    object StringEnd : JsonEvent { override fun toString(): String = "StringEnd" }

    @JvmInline
    value class NumberValue(val value: Number) : JsonEvent { override fun toString(): String = "NumberValue($value)" }

    @JvmInline
    value class BooleanValue(val value: Boolean) : JsonEvent { override fun toString(): String = "BooleanValue($value)" }

    object NullValue : JsonEvent { override fun toString(): String = "NullValue" }

}

class JsonParsingException(msg: String) : RuntimeException(msg)

interface StreamingJsonParser {

    fun parse(chunk: String): List<JsonEvent>

    fun reset()

}

fun Flow<String>.toJsonEvents(
    parser: StreamingJsonParser = DefaultStreamingJsonParser()
): Flow<JsonEvent> = transform { chunk ->
    parser.parse(chunk).forEach { event ->
        emit(event)
    }
}
