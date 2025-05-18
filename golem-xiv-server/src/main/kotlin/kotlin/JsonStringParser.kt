/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.kotlin

class JsonStringParser {
    // Parser state
    private enum class State {
        EXPECT_INITIAL_QUOTE,
        PARSING_STRING,
        PARSING_ESCAPE,
        PARSING_UNICODE,
        COMPLETED
    }

    private var state = State.EXPECT_INITIAL_QUOTE
    private var unicodeBuffer = ""
    private var textBuffer = StringBuilder()

    fun parse(jsonDelta: String): List<Result> {
        val results = mutableListOf<Result>()

        if (jsonDelta.isEmpty()) {
            return results
        }

        for (i in jsonDelta.indices) {
            val c = jsonDelta[i]

            when (state) {
                State.EXPECT_INITIAL_QUOTE -> {
                    if (c == '"') {
                        state = State.PARSING_STRING
                        results.add(Result.InitialQuote)
                    } else {
                        throw InvalidJsonStringException("Expected opening quote")
                    }
                }

                State.PARSING_STRING -> {
                    when (c) {
                        '"' -> {
                            if (textBuffer.isNotEmpty()) {
                                results.add(Result.StringDelta(textBuffer.toString()))
                                textBuffer.clear()
                            }
                            results.add(Result.FinalQuote)
                            state = State.COMPLETED
                            // Reset for next string
                            reset()
                        }
                        '\\' -> {
                            if (textBuffer.isNotEmpty()) {
                                results.add(Result.StringDelta(textBuffer.toString()))
                                textBuffer.clear()
                            }
                            state = State.PARSING_ESCAPE
                            results.add(Result.Collecting)
                        }
                        else -> {
                            textBuffer.append(c)
                        }
                    }
                }

                State.PARSING_ESCAPE -> {
                    when (c) {
                        '"', '\\', '/' -> {
                            results.add(Result.StringDelta(c.toString()))
                            state = State.PARSING_STRING
                        }
                        'b' -> {
                            results.add(Result.StringDelta("\b"))
                            state = State.PARSING_STRING
                        }
                        'f' -> {
                            results.add(Result.StringDelta("\u000C"))
                            state = State.PARSING_STRING
                        }
                        'n' -> {
                            results.add(Result.StringDelta("\n"))
                            state = State.PARSING_STRING
                        }
                        'r' -> {
                            results.add(Result.StringDelta("\r"))
                            state = State.PARSING_STRING
                        }
                        't' -> {
                            results.add(Result.StringDelta("\t"))
                            state = State.PARSING_STRING
                        }
                        'u' -> {
                            state = State.PARSING_UNICODE
                            unicodeBuffer = ""
                            results.add(Result.Collecting)
                        }
                        else -> {
                            throw InvalidJsonStringException("Invalid escape sequence: \\$c")
                        }
                    }
                }

                State.PARSING_UNICODE -> {
                    unicodeBuffer += c

                    if (unicodeBuffer.length < 4) {
                        // Keep collecting Unicode digits
                        results.add(Result.Collecting)
                    } else {
                        // Process complete Unicode escape
                        try {
                            val codePoint = unicodeBuffer.toInt(16)
                            results.add(Result.StringDelta(codePoint.toChar().toString()))
                            state = State.PARSING_STRING
                        } catch (e: NumberFormatException) {
                            throw InvalidJsonStringException("Invalid Unicode escape: \\u$unicodeBuffer")
                        }
                    }
                }

                State.COMPLETED -> {
                    // Start a new string
                    reset()
                    // Process the current character
                    if (c == '"') {
                        state = State.PARSING_STRING
                        results.add(Result.InitialQuote)
                    } else {
                        throw InvalidJsonStringException("Expected opening quote")
                    }
                }
            }
        }

        // Flush any buffered content before returning
        if (state == State.PARSING_STRING && textBuffer.isNotEmpty()) {
            results.add(Result.StringDelta(textBuffer.toString()))
            textBuffer.clear()
        }

        return results
    }

    private fun reset() {
        state = State.EXPECT_INITIAL_QUOTE
        unicodeBuffer = ""
        textBuffer.clear()
    }

    sealed interface Result {
        object InitialQuote: Result { override fun toString(): String = "InitialQuote" }
        object Collecting: Result { override fun toString(): String = "Collecting" }
        @JvmInline
        value class StringDelta(val text: String) : Result { override fun toString(): String = "StringDelta[$text]" }
        object FinalQuote: Result { override fun toString(): String = "FinalQuote" }
    }

}

class InvalidJsonStringException(
    msg: String
) : RuntimeException(msg)
