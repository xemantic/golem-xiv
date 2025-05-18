/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.kotlin

import com.xemantic.kotlin.test.assert
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class IncrementalJsonParserTest {

    @Test
    fun `should not parse empty JSON chunk`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = ""

        // when
        val events = parser.parse(input)

        // then
        assert(events == emptyList<JsonEvent>())
    }

    @Test
    fun `should parse null value`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = "null"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.NullValue
        ))
    }

    @Test
    fun `should parse boolean values`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val trueInput = "true"
        val falseInput = "false"

        // when
        val trueEvents = parser.parse(trueInput)
        val falseEvents = parser.parse(falseInput)

        // then
        assert(trueEvents == listOf(
            JsonEvent.BooleanValue(true)
        ))

        assert(falseEvents == listOf(
            JsonEvent.BooleanValue(false)
        ))
    }

    @Test
    fun `should parse number value`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = "42"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.NumberValue(42)
        ))
    }

    @Test
    fun `should parse decimal number value`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = "3.14"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.NumberValue(3.14)
        ))
    }

    @Test
    fun `should parse scientific notation number`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = "1.23e+4"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.NumberValue(12300.0)
        ))
    }

    @Test
    fun `should parse string value`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = "\"hello world\""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.StringStart,
            JsonEvent.StringDelta("hello world"),
            JsonEvent.StringEnd
        ))
    }

    @Test
    fun `should parse string with escape sequences`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = "\"hello\\nworld\\\"quote\\\\\""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.StringStart,
            JsonEvent.StringDelta("hello\nworld\"quote\\"),
            JsonEvent.StringEnd
        ))
    }

    @Test
    fun `should parse empty object`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = "{}"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.ObjectStart,
            JsonEvent.ObjectEnd
        ))
    }

    @Test
    fun `should parse object with single property`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = """{"name":"John"}"""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("name"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("John"),
            JsonEvent.StringEnd,
            JsonEvent.ObjectEnd
        ))
    }

    @Test
    fun `should parse object with multiple properties`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = """{"name":"John","age":30,"isActive":true}"""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("name"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("John"),
            JsonEvent.StringEnd,
            JsonEvent.PropertyName("age"),
            JsonEvent.NumberValue(30),
            JsonEvent.PropertyName("isActive"),
            JsonEvent.BooleanValue(true),
            JsonEvent.ObjectEnd,
        ))
    }

    @Test
    fun `should parse empty array`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = "[]"

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.ArrayStart,
            JsonEvent.ArrayEnd
        ))
    }

    @Test
    fun `should parse array with simple values`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = """[1,"two",true,null]"""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.ArrayStart,
            JsonEvent.NumberValue(1),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("two"),
            JsonEvent.StringEnd,
            JsonEvent.BooleanValue(true),
            JsonEvent.NullValue,
            JsonEvent.ArrayEnd
        ))
    }

    @Test
    fun `should parse nested objects and arrays`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = """{"person":{"name":"John","hobbies":["reading",{"type":"sport","name":"running"}]}}"""

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("person"),
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("name"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("John"),
            JsonEvent.StringEnd,
            JsonEvent.PropertyName("hobbies"),
            JsonEvent.ArrayStart,
            JsonEvent.StringStart,
            JsonEvent.StringDelta("reading"),
            JsonEvent.StringEnd,
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("type"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("sport"),
            JsonEvent.StringEnd,
            JsonEvent.PropertyName("name"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("running"),
            JsonEvent.StringEnd,
            JsonEvent.ObjectEnd,
            JsonEvent.ArrayEnd,
            JsonEvent.ObjectEnd,
            JsonEvent.ObjectEnd
        ))
    }

    @Test
    fun `should parse JSON with whitespace`() = runTest {
        // given
        val parser = IncrementalJsonParser()
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
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("name"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("John"),
            JsonEvent.StringEnd,
            JsonEvent.PropertyName("age"),
            JsonEvent.NumberValue(30),
            JsonEvent.ObjectEnd
        ))
    }

    @Test
    fun `should handle incremental parsing of object`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val chunk1 = """{"na"""
        val chunk2 = """me":"Jo"""
        val chunk3 = """hn"}"""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)
        val events3 = parser.parse(chunk3)

        // then
        assert(events1 == listOf(
            JsonEvent.ObjectStart
        ))

        assert(events2 == listOf(
            JsonEvent.PropertyName("name"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("Jo")
        ))

        assert(events3 == listOf(
            JsonEvent.StringDelta("hn"),
            JsonEvent.StringEnd,
            JsonEvent.ObjectEnd
        ))
    }

    @Test
    fun `should handle string split across chunks`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val chunk1 = "\"Hello"
        val chunk2 = " World\""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            JsonEvent.StringStart,
            JsonEvent.StringDelta("Hello")
        ))

        assert(events2 == listOf(
            JsonEvent.StringDelta(" World"),
            JsonEvent.StringEnd
        ))
    }

    @Test
    fun `should handle escape sequence split across chunks`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val chunk1 = "\"Hello\\"
        val chunk2 = "nWorld\""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            JsonEvent.StringStart,
            JsonEvent.StringDelta("Hello")
        ))

        assert(events2 == listOf(
            JsonEvent.StringDelta("\nWorld"),
            JsonEvent.StringEnd
        ))
    }

    @Test
    fun `should handle property name split across chunks`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val chunk1 = """{"prop"""
        val chunk2 = """ertyN"""
        val chunk3 = """ame":42}"""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)
        val events3 = parser.parse(chunk3)

        // then
        assert(events1 == listOf(
            JsonEvent.ObjectStart
        ))

        assert(events2 == emptyList<JsonEvent>())

        assert(events3 == listOf(
            JsonEvent.PropertyName("propertyName"),
            JsonEvent.NumberValue(42),
            JsonEvent.ObjectEnd
        ))
    }

    @Test
    fun `should handle complex JSON split across multiple chunks`() = runTest {
        // given
        val parser = IncrementalJsonParser()
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
        // Verifying the structure without listing all events for brevity
        assert(events1 == listOf(
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("menu"),
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("id"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("fi")
        ))

        // Verify the first complete object is emitted in the last chunk
        assert(events4.contains(JsonEvent.ObjectEnd))
    }

    @Test
    fun `should throw exception for invalid JSON syntax`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = """{"name":"John",}""" // Extra comma is invalid

        // when/then
        assertFailsWith<JsonParsingException> {
            parser.parse(input)
        }
    }

    @Test
    fun `should throw exception for mismatched brackets`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val chunk1 = """{"name":"John"}"""
        val chunk2 = "}" // Extra closing brace

        // when/then
        parser.parse(chunk1) // This should parse correctly

        assertFailsWith<JsonParsingException> {
            parser.parse(chunk2) // This should throw an exception
        }
    }

    @Test
    fun `should throw exception for invalid property format`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val chunk1 = """{"name" """
        val chunk2 = """  "John"}""" // Missing colon

        // when/then
        parser.parse(chunk1) // This might parse without error

        assertFailsWith<JsonParsingException> {
            parser.parse(chunk2) // This should throw an exception
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
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("name"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("John"),
            JsonEvent.StringEnd,
            JsonEvent.PropertyName("age"),
            JsonEvent.NumberValue(30),
            JsonEvent.ObjectEnd
        ))
    }

    @Test
    fun `should handle unicode characters in strings`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val input = "\"Hello \\u00A9 World\"" // Copyright symbol

        // when
        val events = parser.parse(input)

        // then
        assert(events == listOf(
            JsonEvent.StringStart,
            JsonEvent.StringDelta("Hello © World"),
            JsonEvent.StringEnd
        ))
    }

    @Test
    fun `should handle unicode character split across chunks`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val chunk1 = "\"Hello \\u00"
        val chunk2 = "A9 World\""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            JsonEvent.StringStart,
            JsonEvent.StringDelta("Hello ")
        ))

        assert(events2 == listOf(
            JsonEvent.StringDelta("© World"),
            JsonEvent.StringEnd
        ))
    }

    @Test
    fun `should handle array split across chunks`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val chunk1 = "[1,2,"
        val chunk2 = "3,4]"

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            JsonEvent.ArrayStart,
            JsonEvent.NumberValue(1),
            JsonEvent.NumberValue(2)
        ))

        assert(events2 == listOf(
            JsonEvent.NumberValue(3),
            JsonEvent.NumberValue(4),
            JsonEvent.ArrayEnd
        ))
    }

    @Test
    fun `should handle nested structures split across chunks`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val chunk1 = """{"users":[{"id":1,"name":"""
        val chunk2 = """"Alice"},{"id":2,"name":"Bob"}]}"""

        // when
        val events1 = parser.parse(chunk1)
        val events2 = parser.parse(chunk2)

        // then
        assert(events1 == listOf(
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("users"),
            JsonEvent.ArrayStart,
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("id"),
            JsonEvent.NumberValue(1),
            JsonEvent.PropertyName("name"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("Alice")
        ))

        assert(events2 == listOf(
            JsonEvent.StringEnd,
            JsonEvent.ObjectEnd,
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("id"),
            JsonEvent.NumberValue(2),
            JsonEvent.PropertyName("name"),
            JsonEvent.StringStart,
            JsonEvent.StringDelta("Bob"),
            JsonEvent.StringEnd,
            JsonEvent.ObjectEnd,
            JsonEvent.ArrayEnd,
            JsonEvent.ObjectEnd
        ))
    }

    @Test
    fun `should resume parsing after throwing exception`() = runTest {
        // given
        val parser = IncrementalJsonParser()
        val invalidChunk = """{"invalid": ["""
        val invalidCompletion = """}""" // Missing array end
        val validChunk = """{"valid": true}"""

        // when/then
        parser.parse(invalidChunk)

        assertFailsWith<JsonParsingException> {
            parser.parse(invalidCompletion)
        }

        // After exception, parser should reset and be able to parse new valid input
        val events = parser.parse(validChunk)

        assert(events == listOf(
            JsonEvent.ObjectStart,
            JsonEvent.PropertyName("valid"),
            JsonEvent.BooleanValue(true),
            JsonEvent.ObjectEnd
        ))
    }
}
