/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.kotlin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

sealed interface JsonEvent {

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

class IncrementalJsonParser {

    fun parse(chunk: String): List<JsonEvent> {
        TODO()
    }

}

fun Flow<String>.toJsonEvents(): Flow<JsonEvent> {
    val parser = IncrementalJsonParser()
    return transform { chunk ->
        parser.parse(chunk).forEach { event ->
            emit(event)
        }
    }
}
