/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.kotlin

import com.xemantic.ai.golem.server.kotlin.JsonEvent.*
import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class StreamingJsonParserTest {

    @Test
    fun `should not parse empty JSON chunk`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = ""

        // when
        val events = parser.parse(input)

        // then
        assert(events == emptyList<JsonEvent>())
    }

    @Test
    fun `should parse null`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "null"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            NullValue,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse true`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "true"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            BooleanValue(true),
            DocumentEnd
        ))
    }

    @Test
    fun `should parse false`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "false"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            BooleanValue(false),
            DocumentEnd
        ))
    }

    @Test
    fun `should parse integer`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "42"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            NumberValue(42L),
            DocumentEnd
        ))
    }

    @Test
    fun `should parse decimal`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "3.14"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            NumberValue(3.14),
            DocumentEnd
        ))
    }

    @Test
    fun `should parse scientific notation number`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "1.23e+4"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            NumberValue(12300.0),
            DocumentEnd
        ))
    }

    @Test
    fun `should parse string`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "\"foo\""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            StringStart,
            StringDelta("foo"),
            StringEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should not parse invalid value`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "foo"

        assertFailsWith<JsonParsingException> {
            // when
            parser.parse(input)

            // then
        } should {
            have(message == "Expected JSON value but found unquoted literal 'foo' at position 0")
        }
    }

    @Test
    fun `should parse empty JSON object`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "{}"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse empty JSON object spread across multiple lines`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = """
            {
            
            }
        """".trimIndent()

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse empty JSON array`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "[]"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ArrayStart,
            ArrayEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse empty JSON array spread across multiple lines`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = """
            [
            
            ]
        """.trimIndent()

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ArrayStart,
            ArrayEnd,
            DocumentEnd
        ))
    }


    @Test
    fun `should continue parsing after empty JSON chunk`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = ""
        val subsequent = "{\"foo\":\"bar\"}"

        // when
        val events1 = parser.parse(input)

        // then
        assert(events1 == emptyList<JsonEvent>())

        // when
        val events2 = parser.parse(subsequent)

        // then
        assert(events2 == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("foo"),
            StringStart,
            StringDelta("bar"),
            StringEnd,
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse boolean properties`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "{\"foo\":true,\"bar\":false}"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("foo"),
            BooleanValue(true),
            PropertyName("bar"),
            BooleanValue(false),
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse integer property`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "{\"foo\":42}"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("foo"),
            NumberValue(42L),
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse decimal property`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "{\"foo\":3.14}"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("foo"),
            NumberValue(3.14),
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse scientific notation number property`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "{\"foo\":1.23e+4}"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("foo"),
            NumberValue(12300.0),
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse string property`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "{\"foo\":\"bar\"}"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("foo"),
            StringStart,
            StringDelta("bar"),
            StringEnd,
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse null value property`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "{\"foo\":null}"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("foo"),
            NullValue,
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse string with escape sequences`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "\"hello\\nworld\\\"quote\\\\\""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            StringStart,
            StringDelta("hello\nworld\"quote\\"),
            StringEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse object with multiple properties`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = """{"name":"John","age":30,"isActive":true}"""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("name"),
            StringStart,
            StringDelta("John"),
            StringEnd,
            PropertyName("age"),
            NumberValue(30L),
            PropertyName("isActive"),
            BooleanValue(true),
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse array with simple values`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = """[1,"two",true,null]"""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ArrayStart,
            NumberValue(1L),
            StringStart,
            StringDelta("two"),
            StringEnd,
            BooleanValue(true),
            NullValue,
            ArrayEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse nested objects and arrays`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = """{"person":{"name":"John","hobbies":["reading",{"type":"sport","name":"running"}]}}"""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("person"),
            ObjectStart,
            PropertyName("name"),
            StringStart,
            StringDelta("John"),
            StringEnd,
            PropertyName("hobbies"),
            ArrayStart,
            StringStart,
            StringDelta("reading"),
            StringEnd,
            ObjectStart,
            PropertyName("type"),
            StringStart,
            StringDelta("sport"),
            StringEnd,
            PropertyName("name"),
            StringStart,
            StringDelta("running"),
            StringEnd,
            ObjectEnd,
            ArrayEnd,
            ObjectEnd,
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should parse JSON with whitespace`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = """
            {
                "name": "John",
                "age": 30
            }
        """.trimIndent()

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("name"),
            StringStart,
            StringDelta("John"),
            StringEnd,
            PropertyName("age"),
            NumberValue(30L),
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should handle incremental parsing of object`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = """{"na"""
        val chunk2 = """me":"Jo"""
        val chunk3 = """hn"}"""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)
        val events3 = parser.parse(chunk3)

        // then
        assert(events1 == listOf(
            DocumentStart,
            ObjectStart
        ))

        assert(events2 == listOf(
            PropertyName("name"),
            StringStart,
            StringDelta("Jo")
        ))

        assert(events3 == listOf(
            StringDelta("hn"),
            StringEnd,
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should handle string split across chunks`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = "\"Hello"
        val chunk2 = " World\""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            DocumentStart,
            StringStart,
            StringDelta("Hello")
        ))

        assert(events2 == listOf(
            StringDelta(" World"),
            StringEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should handle escape sequence split across chunks`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = "\"Hello\\"
        val chunk2 = "nWorld\""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            DocumentStart,
            StringStart,
            StringDelta("Hello")
        ))

        assert(events2 == listOf(
            StringDelta("\nWorld"),
            StringEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should handle property name split across chunks`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = """{"prop"""
        val chunk2 = """ertyN"""
        val chunk3 = """ame":42}"""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)
        val events3 = parser.parse(chunk3)

        // then
        assert(events1 == listOf(
            DocumentStart,
            ObjectStart
        ))

        assert(events2 == emptyList<JsonEvent>())

        assert(events3 == listOf(
            PropertyName("propertyName"),
            NumberValue(42L),
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should handle complex JSON split across multiple chunks`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = """{"menu":{"id":"fi"""
        val chunk2 = """le","value":"File","popup":{"men"""
        val chunk3 = """uitems":[{"value":"New","onclick":"CreateNew"},{"val"""
        val chunk4 = """ue":"Open","onclick":"OpenDoc"}]}}"""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)
        val events3 = parser.parse(chunk3)
        val events4 = parser.parse(chunk4)

        // then
        assert(events1 == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("menu"),
            ObjectStart,
            PropertyName("id"),
            StringStart,
            StringDelta("fi")
        ))

        assert(events2 == listOf(
            StringDelta("le"),
            StringEnd,
            PropertyName("value"),
            StringStart,
            StringDelta("File"),
            StringEnd,
            PropertyName("popup"),
            ObjectStart
        ))

        assert(events3 == listOf(
            PropertyName("menuitems"),
            ArrayStart,
            ObjectStart,
            PropertyName("value"),
            StringStart,
            StringDelta("New"),
            StringEnd,
            PropertyName("onclick"),
            StringStart,
            StringDelta("CreateNew"),
            StringEnd,
            ObjectEnd,
            ObjectStart
        ))

        assert(events4 == listOf(
            PropertyName("value"),
            StringStart,
            StringDelta("Open"),
            StringEnd,
            PropertyName("onclick"),
            StringStart,
            StringDelta("OpenDoc"),
            StringEnd,
            ObjectEnd,
            ArrayEnd,
            ObjectEnd,
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should throw exception for invalid JSON syntax`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = """{"name":"John",}""" // Extra comma is invalid

        assertFailsWith<JsonParsingException> {
            // when
            parser.parse(input)

            // then
        } should {
            have(message == "Unexpected token ',' at position 15: object entries must be followed by a property")
        }
    }

    @Test
    fun `should throw exception for mismatched brackets`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = """{"name":"John"}"""
        val chunk2 = "}" // Extra closing brace
        parser.parse(chunk1) // This should parse correctly

        assertFailsWith<JsonParsingException> {
            // when
            parser.parse(chunk2)

            // then
        } should {
            have(message == "Unexpected token '}' after JSON document was already complete")
        }
    }

    @Test
    fun `should not throw exception for trailing whitespaces`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = """{"name":"John"}"""
        parser.parse(chunk1) // This should parse correctly
        val chunk2 = " \n \t"

        // when
        val events = parser.parse(chunk2)

        // then
        assert(events == emptyList<JsonEvent>())
    }

    @Test
    fun `should throw exception for invalid property format`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = """{"name" """
        parser.parse(chunk1) // This should parse without error
        val chunk2 = """  "John"}""" // Missing colon

        assertFailsWith<JsonParsingException> {
            // when
            parser.parse(chunk2) // This should throw an exception

            // then
        } should {
            have(message == "Expected ':' after property name, but found string literal at position 10")
        }
    }

    @Test
    fun `should convert Flow of chunks to Flow of events`() = runTest {
        // given
        val chunks = flowOf(
            """{"name":""",
            """"John","age":""",
            """30}"""
        )

        // when
        val events = chunks.toJsonEvents().toList()

        // then
        assert(events == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("name"),
            StringStart,
            StringDelta("John"),
            StringEnd,
            PropertyName("age"),
            NumberValue(30L),
            ObjectEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should handle unicode characters in strings`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val input = "\"Hello \\u00A9 World\"" // Copyright symbol

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            DocumentStart,
            StringStart,
            StringDelta("Hello © World"),
            StringEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should handle unicode character split across chunks`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = "\"Hello \\u00"
        val chunk2 = "A9 World\""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            DocumentStart,
            StringStart,
            StringDelta("Hello ")
        ))

        assert(events2 == listOf(
            StringDelta("© World"),
            StringEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should handle array split across chunks`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = "[1,2,"
        val chunk2 = "3,4]"

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            DocumentStart,
            ArrayStart,
            NumberValue(1L),
            NumberValue(2L)
        ))

        assert(events2 == listOf(
            NumberValue(3L),
            NumberValue(4L),
            ArrayEnd,
            DocumentEnd
        ))
    }

    @Test
    fun `should handle nested structures split across chunks`() {
        // given
        val parser = DefaultStreamingJsonParser()
        val chunk1 = """{"users":[{"id":1,"name":"""
        val chunk2 = """"Alice"},{"id":2,"name":"Bob"}]}"""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            DocumentStart,
            ObjectStart,
            PropertyName("users"),
            ArrayStart,
            ObjectStart,
            PropertyName("id"),
            NumberValue(1L),
            PropertyName("name"),
        ))

        assert(events2 == listOf(
            StringStart,
            StringDelta("Alice"),
            StringEnd,
            ObjectEnd,
            ObjectStart,
            PropertyName("id"),
            NumberValue(2L),
            PropertyName("name"),
            StringStart,
            StringDelta("Bob"),
            StringEnd,
            ObjectEnd,
            ArrayEnd,
            ObjectEnd,
            DocumentEnd
        ))
    }

}
