/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.json

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlin.test.Test
import kotlin.test.assertFailsWith

class JsonStringParserTest {

    @Test
    fun `should parse simple string`() {
        // given
        val parser = JsonStringParser()

        // when
        val result = parser.parse("\"foo\"")

        // then
        assert(result == listOf(
            JsonStringParser.Result.InitialQuote,
            JsonStringParser.Result.StringDelta("foo"),
            JsonStringParser.Result.FinalQuote
        ))
    }

    @Test
    fun `should handle opening quote in separate increment`() {
        // given
        val parser = JsonStringParser()

        // when
        val result1 = parser.parse("\"")
        val result2 = parser.parse("foo\"")

        // then
        assert(result1 == listOf(JsonStringParser.Result.InitialQuote))
        assert(result2 == listOf(
            JsonStringParser.Result.StringDelta("foo"),
            JsonStringParser.Result.FinalQuote
        ))
    }

    @Test
    fun `should handle multiple content increments`() {
        // given
        val parser = JsonStringParser()

        // when
        val result1 = parser.parse("\"")
        val result2 = parser.parse("part1")
        val result3 = parser.parse(" part2")
        val result4 = parser.parse(" part3\"")

        // then
        assert(result1 == listOf(JsonStringParser.Result.InitialQuote))
        assert(result2 == listOf(JsonStringParser.Result.StringDelta("part1")))
        assert(result3 == listOf(JsonStringParser.Result.StringDelta(" part2")))
        assert(result4 == listOf(
            JsonStringParser.Result.StringDelta(" part3"),
            JsonStringParser.Result.FinalQuote
        ))
    }

    @Test
    fun `should handle escape sequence split across increments`() {
        // given
        val parser = JsonStringParser()

        // when
        val result1 = parser.parse("\"foo\\")
        val result2 = parser.parse("\"bar\"")

        // then
        assert(result1 == listOf(
            JsonStringParser.Result.InitialQuote,
            JsonStringParser.Result.StringDelta("foo"),
            JsonStringParser.Result.Collecting
        ))
        assert(result2 == listOf(
            JsonStringParser.Result.StringDelta("\""),
            JsonStringParser.Result.StringDelta("bar"),
            JsonStringParser.Result.FinalQuote
        ))
    }

    @Test
    fun `should handle Unicode escape split across increments`() {
        // given
        val parser = JsonStringParser()

        // when
        val result1 = parser.parse("\"prefix\\u")
        val result2 = parser.parse("26")
        val result3 = parser.parse("05suffix\"")

        // then
        assert(result1 == listOf(
            JsonStringParser.Result.InitialQuote,
            JsonStringParser.Result.StringDelta("prefix"),
            JsonStringParser.Result.Collecting,
            JsonStringParser.Result.Collecting
        ))
        assert(result2 == listOf(
            JsonStringParser.Result.Collecting,
            JsonStringParser.Result.Collecting
        ))
        assert(result3 == listOf(
            JsonStringParser.Result.Collecting,
            JsonStringParser.Result.StringDelta("★"),
            JsonStringParser.Result.StringDelta("suffix"),
            JsonStringParser.Result.FinalQuote
        ))
    }

    @Test
    fun `should handle multiple escape sequences across increments`() {
        // given
        val parser = JsonStringParser()

        // when
        val result1 = parser.parse("\"start\\")
        val result2 = parser.parse("n\\")
        val result3 = parser.parse("tmiddle\\")
        val result4 = parser.parse("\"end\"")

        // then
        assert(result1 == listOf(
            JsonStringParser.Result.InitialQuote,
            JsonStringParser.Result.StringDelta("start"),
            JsonStringParser.Result.Collecting
        ))
        assert(result2 == listOf(
            JsonStringParser.Result.StringDelta("\n"),
            JsonStringParser.Result.Collecting
        ))
        assert(result3 == listOf(
            JsonStringParser.Result.StringDelta("\t"),
            JsonStringParser.Result.StringDelta("middle"),
            JsonStringParser.Result.Collecting
        ))
        assert(result4 == listOf(
            JsonStringParser.Result.StringDelta("\""),
            JsonStringParser.Result.StringDelta("end"),
            JsonStringParser.Result.FinalQuote
        ))
    }

    @Test
    fun `should fail on invalid escape sequence in increment`() {
        // given
        val parser = JsonStringParser()

        // when/then
        parser.parse("\"start\\")
        assertFailsWith<InvalidJsonStringException> {
            parser.parse("z")
        } should {
            have(message == "Invalid escape sequence: \\z")
        }
    }

    @Test
    fun `should handle empty increments`() {
        // given
        val parser = JsonStringParser()

        // when
        val result1 = parser.parse("\"")
        val result2 = parser.parse("")
        val result3 = parser.parse("content\"")

        // then
        assert(result1 == listOf(JsonStringParser.Result.InitialQuote))
        assert(result2.isEmpty()) // Empty increment, no results
        assert(result3 == listOf(
            JsonStringParser.Result.StringDelta("content"),
            JsonStringParser.Result.FinalQuote
        ))
    }

    @Test
    fun `should handle incomplete JSON at end of stream`() {
        // given
        val parser = JsonStringParser()

        // when/then
        parser.parse("\"incomplete")
        // This test verifies that the parser doesn't throw an exception for incomplete JSON
        // The exception should only be thrown when attempting to access incomplete results
        // or when we explicitly signal end-of-stream (which would be a separate method)
    }

    @Test
    fun `should process multiple strings sequentially`() {
        // given
        val parser = JsonStringParser()

        // when - first string
        val result1 = parser.parse("\"first\"")

        // then
        assert(result1 == listOf(
            JsonStringParser.Result.InitialQuote,
            JsonStringParser.Result.StringDelta("first"),
            JsonStringParser.Result.FinalQuote
        ))

        // when - second string
        val result2 = parser.parse("\"second\"")

        // then
        assert(result2 == listOf(
            JsonStringParser.Result.InitialQuote,
            JsonStringParser.Result.StringDelta("second"),
            JsonStringParser.Result.FinalQuote
        ))
    }

}
