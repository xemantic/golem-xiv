/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.kotlin

internal fun Char.isHexDigit(): Boolean {
    return this in '0'..'9' ||
            this in 'a'..'f' ||
            this in 'A'..'F'
}

class DefaultStreamingJsonParser : StreamingJsonParser {

    private var buffer = StringBuilder()
    private var unicodeHexBuffer = StringBuilder()
    private var unicodeCharsLeft = 0

    private val contextStack = mutableListOf<ContextNode>()
    private var state = State.EXPECTING_DOCUMENT_START

    private var parsingStringValue = false // True if current string is a JSON value (emits StringStart/Delta/End)

    private enum class State {
        EXPECTING_DOCUMENT_START,
        IN_OBJECT_EXPECTING_KEY_OR_END,    // After '{' or after a comma in an object
        IN_OBJECT_EXPECTING_COLON,         // After a key string
        IN_OBJECT_EXPECTING_VALUE,         // After ':'
        IN_OBJECT_EXPECTING_COMMA_OR_END,  // After an object value
        IN_ARRAY_EXPECTING_ELEMENT_OR_END, // After '[' or after a comma in an array
        IN_ARRAY_EXPECTING_COMMA_OR_END,   // After an array element

        IN_STRING,
        IN_STRING_ESCAPE,
        IN_STRING_UNICODE_ESCAPE,

        EXPECTING_LITERAL_VALUE, // For numbers, true, false, null (accumulating in buffer)

        FINISHED_DOCUMENT
    }

    private enum class ContextType { OBJECT, ARRAY }
    private data class ContextNode(val type: ContextType, var hasElements: Boolean = false)

    override fun parse(chunk: String): List<JsonEvent> {
        val events = mutableListOf<JsonEvent>()
        for (char in chunk) {
            processChar(char, events)
        }

        if (state == State.IN_STRING && parsingStringValue && buffer.isNotEmpty()) {
            events.add(JsonEvent.StringDelta(buffer.toString()))
            buffer.clear()
        }
        return events
    }

    override fun reset() {
        buffer.clear()
        unicodeHexBuffer.clear()
        unicodeCharsLeft = 0
        contextStack.clear()
        state = State.EXPECTING_DOCUMENT_START
        parsingStringValue = false
    }

    private fun processChar(char: Char, events: MutableList<JsonEvent>) {
        if (char.isWhitespace()) {
            if (state == State.IN_STRING) { // Whitespace is part of string content
                buffer.append(char)
            } else if (state == State.EXPECTING_LITERAL_VALUE && buffer.isNotEmpty()) {
                // Whitespace ends a buffered literal (number, true, false, null)
                processBufferedLiteral(events)
                // State transition is handled by processBufferedLiteral
            }
            // In other states, whitespace is generally ignored.
            return
        }

        when (state) {
            State.IN_STRING -> handleInString(char, events)
            State.IN_STRING_ESCAPE -> handleInStringEscape(char, events)
            State.IN_STRING_UNICODE_ESCAPE -> handleInStringUnicode(char, events)
            State.EXPECTING_LITERAL_VALUE -> handleExpectedLiteral(char, events)
            State.EXPECTING_DOCUMENT_START -> {
                if (buffer.isNotEmpty()) throw JsonParsingException("Data before document start.")
                startValueParsing(char, events)
            }
            State.IN_OBJECT_EXPECTING_KEY_OR_END -> {
                when (char) {
                    '"' -> { // Start of key
                        state = State.IN_STRING
                        parsingStringValue = false // It's a key
                        buffer.clear()
                    }
                    '}' -> { // End of object
                        if (contextStack.lastOrNull()?.hasElements == true) { // e.g. {"k":"v",}
                            throw JsonParsingException("Trailing comma in object.")
                        }
                        endContext(events, ContextType.OBJECT)
                    }
                    else -> throw JsonParsingException("Expected '\"' for key or '}' in object, got '$char'.")
                }
            }
            State.IN_OBJECT_EXPECTING_COLON -> {
                if (char == ':') {
                    state = State.IN_OBJECT_EXPECTING_VALUE
                } else {
                    throw JsonParsingException("Expected ':' after key, got '$char'.")
                }
            }
            State.IN_OBJECT_EXPECTING_VALUE -> startValueParsing(char, events)
            State.IN_OBJECT_EXPECTING_COMMA_OR_END -> {
                when (char) {
                    ',' -> {
                        contextStack.lastOrNull()?.hasElements = true
                        state = State.IN_OBJECT_EXPECTING_KEY_OR_END
                    }
                    '}' -> endContext(events, ContextType.OBJECT)
                    else -> throw JsonParsingException("Expected ',' or '}' in object, got '$char'.")
                }
            }
            State.IN_ARRAY_EXPECTING_ELEMENT_OR_END -> {
                if (char == ']') { // End of array
                    if (contextStack.lastOrNull()?.hasElements == true) { // e.g. [1,]
                        throw JsonParsingException("Trailing comma in array.")
                    }
                    endContext(events, ContextType.ARRAY)
                } else {
                    startValueParsing(char, events) // Start of an element
                }
            }
            State.IN_ARRAY_EXPECTING_COMMA_OR_END -> {
                when (char) {
                    ',' -> {
                        contextStack.lastOrNull()?.hasElements = true
                        state = State.IN_ARRAY_EXPECTING_ELEMENT_OR_END
                    }
                    ']' -> endContext(events, ContextType.ARRAY)
                    else -> throw JsonParsingException("Expected ',' or ']' in array, got '$char'.")
                }
            }
            State.FINISHED_DOCUMENT -> {
                reset() // Prepare for a new document
                processChar(char, events) // Re-process the character in the new initial state
            }
        }
    }

    private fun handleInString(char: Char, events: MutableList<JsonEvent>) {
        when (char) {
            '"' -> { // End of string
                if (parsingStringValue) {
                    if (buffer.isNotEmpty()) events.add(JsonEvent.StringDelta(buffer.toString()))
                    events.add(JsonEvent.StringEnd)
                    buffer.clear()
                    parsingStringValue = false
                    transitionAfterValue()
                } else { // End of an object key
                    events.add(JsonEvent.PropertyName(buffer.toString()))
                    buffer.clear()
                    state = State.IN_OBJECT_EXPECTING_COLON
                }
            }
            '\\' -> {
                if (parsingStringValue && buffer.isNotEmpty()) {
                    events.add(JsonEvent.StringDelta(buffer.toString()))
                    buffer.clear()
                }
                state = State.IN_STRING_ESCAPE
            }
            else -> buffer.append(char) // Accumulate char in buffer
        }
    }

    private fun handleInStringEscape(char: Char, events: MutableList<JsonEvent>) {
        val escapedChar = when (char) {
            '"', '\\', '/' -> char
            'b' -> '\b'
            'f' -> '\u000C' // Form Feed
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'u' -> {
                state = State.IN_STRING_UNICODE_ESCAPE
                unicodeCharsLeft = 4
                unicodeHexBuffer.clear()
                return // unicode processing will handle buffer append
            }
            else -> throw JsonParsingException("Invalid escape sequence: \\$char")
        }
        buffer.append(escapedChar)
        state = State.IN_STRING
    }

    private fun handleInStringUnicode(char: Char, events: MutableList<JsonEvent>) {
        if (!char.isHexDigit()) throw JsonParsingException("Invalid hex char '$char' in Unicode escape.")
        unicodeHexBuffer.append(char)
        unicodeCharsLeft--
        if (unicodeCharsLeft == 0) {
            buffer.append(unicodeHexBuffer.toString().toInt(16).toChar())
            state = State.IN_STRING
        }
    }

    private fun handleExpectedLiteral(char: Char, events: MutableList<JsonEvent>) {
        when (char) {
            // Characters that can continue a number or literal
            in '0'..'9', '.', 'e', 'E', '+', '-',
            in 'a'..'z' // for true, false, null
                -> buffer.append(char)
            // Characters that delimit a literal
            ',', '}', ']' -> {
                processBufferedLiteral(events)
                processChar(char, events) // Re-process delimiter in new state
            }
            else -> throw JsonParsingException("Unexpected char '$char' while parsing literal: '${buffer}'")
        }
    }

    private fun startValueParsing(char: Char, events: MutableList<JsonEvent>) {
        if (buffer.isNotEmpty()) { // Should have been processed if it was a literal ending
            processBufferedLiteral(events)
            // The current `char` might be a delimiter that was already consumed by `processBufferedLiteral` logic or re-processed
            // This needs to be robust. Let's assume buffer is clear when startValueParsing is called, or char is part of new value.
        }
        buffer.clear() // Ensure buffer is clear for new value

        when (char) {
            '{' -> {
                events.add(JsonEvent.ObjectStart)
                contextStack.add(ContextNode(ContextType.OBJECT))
                state = State.IN_OBJECT_EXPECTING_KEY_OR_END
            }
            '[' -> {
                events.add(JsonEvent.ArrayStart)
                contextStack.add(ContextNode(ContextType.ARRAY))
                state = State.IN_ARRAY_EXPECTING_ELEMENT_OR_END
            }
            '"' -> {
                events.add(JsonEvent.StringStart)
                state = State.IN_STRING
                parsingStringValue = true
            }
            // Potential start of true, false, null, or number
            't', 'f', 'n', '-', in '0'..'9' -> {
                buffer.append(char)
                state = State.EXPECTING_LITERAL_VALUE
            }
            else -> throw JsonParsingException("Unexpected character '$char' starting a value.")
        }
    }

    private fun processBufferedLiteral(events: MutableList<JsonEvent>) {
        if (buffer.isEmpty()) return

        val token = buffer.toString()
        buffer.clear()

        when (token) {
            "true" -> events.add(JsonEvent.BooleanValue(true))
            "false" -> events.add(JsonEvent.BooleanValue(false))
            "null" -> events.add(JsonEvent.NullValue)
            else -> { // Number
                try {
                    val numValue: Number = if (token.any { it == '.' || it == 'e' || it == 'E' }) {
                        token.toDouble()
                    } else {
                        token.toLong()
                    }
                    events.add(JsonEvent.NumberValue(numValue))
                } catch (e: NumberFormatException) {
                    throw JsonParsingException("Invalid numeric literal: '$token'")
                }
            }
        }
        transitionAfterValue()
    }

    private fun transitionAfterValue() {
        val currentContext = contextStack.lastOrNull()
        if (currentContext == null) { // Top-level value
            state = State.FINISHED_DOCUMENT
        } else {
            currentContext.hasElements = true
            state = when (currentContext.type) {
                ContextType.OBJECT -> State.IN_OBJECT_EXPECTING_COMMA_OR_END
                ContextType.ARRAY -> State.IN_ARRAY_EXPECTING_COMMA_OR_END
            }
        }
    }

    private fun endContext(events: MutableList<JsonEvent>, expectedType: ContextType) {
        val context = contextStack.lastOrNull()
        if (context == null || context.type != expectedType) {
            throw JsonParsingException("Mismatched end of ${expectedType.name.lowercase()}.")
        }

        when (expectedType) {
            ContextType.OBJECT -> events.add(JsonEvent.ObjectEnd)
            ContextType.ARRAY -> events.add(JsonEvent.ArrayEnd)
        }
        contextStack.removeLast()
        transitionAfterValue() // An object/array is also a value in its parent context
    }

}
