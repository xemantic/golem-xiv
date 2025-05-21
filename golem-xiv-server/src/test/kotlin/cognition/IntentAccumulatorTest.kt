/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.cognition

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.be
import com.xemantic.kotlin.test.should
import kotlin.test.Test
import kotlin.test.assertFailsWith

class IntentAccumulatorTest {

    @Test
    fun `should parse complete intent in one go`() {
        // given
        val accumulator = IntentBroadcaster()
        val json = """{"purpose": "Reading /etc/passwd", "code": "files.read(\"/etc/passwd\")"}"""

        // when
        val result = accumulator.add(json)

        // then
        result should {
            be<IntentBroadcaster.Result.CodeCulminated>()
        }
    }

    @Test
    fun `should parse intent incrementally`() {
        // given
        val accumulator = IntentBroadcaster()

        // when - opening brace
        val result1 = accumulator.add("{")

        // then
        //assert(result1 is IntentAccumulator.Result.Collecting)

        // when - purpose field begins
        val result2 = accumulator.add(""""purpose": "Reading /etc/passwd"""")

        // then
        assert(result2 is IntentBroadcaster.Result.Purpose)
        assert(result2.text == "Reading /etc/passwd")

        // when - comma and code field begins
        val result3 = accumulator.add(""", "code": "files""")

        // then
        assert(result3 is IntentBroadcaster.Result.CodeDelta)
        assert(result3.delta == "files")

        // when - code field continues
        val result4 = accumulator.add(""".read(\"""")

        // then
        assert(result4 is IntentBroadcaster.Result.CodeDelta)
        assert(result4.delta == ".read(\"")

        // when - code field continues with escaped quote
        val result5 = accumulator.add("""/etc/passwd\"""")

        // then
        assert(result5 is IntentBroadcaster.Result.CodeDelta)
        assert(result5.delta == "/etc/passwd\"")

        // when - code field ends and JSON ends
        val result6 = accumulator.add(""")"}""")

        // then
        assert(result6 is IntentBroadcaster.Result.CodeCulminated)
    }

    @Test
    fun `should handle whitespace in JSON`() {
        // given
        val accumulator = IntentBroadcaster()
        val json = """
        {
          "purpose": "Reading /etc/passwd",
          "code": "files.read(\"/etc/passwd\")"
        }
        """.trimIndent()

        // when
        val result = accumulator.add(json)

        // then
        assert(result is IntentBroadcaster.Result.CodeCulminated)
    }

    @Test
    fun `should handle purpose with escape sequences`() {
        // given
        val accumulator = IntentBroadcaster()

        // when
        val result1 = accumulator.add("""{"purpose": "Reading file with \\t tab and \\n newline"""")

        // then
        assert(result1 is IntentBroadcaster.Result.Purpose)
        assert(result1.text == "Reading file with \t tab and \n newline")

        // when
        val result2 = accumulator.add(""", "code": "example()"}""")

        // then
        assert(result2 is IntentBroadcaster.Result.CodeCulminated)
    }

    @Test
    fun `should handle code with nested JSON`() {
        // given
        val accumulator = IntentBroadcaster()
        val json = """{"purpose": "Processing data", "code": "processJson({\\"key\\": \\"value\\"})"}"""

        // when
        val result = accumulator.add(json)

        // then
        assert(result is IntentBroadcaster.Result.CodeCulminated)
    }

    @Test
    fun `should handle empty purpose and code`() {
        // given
        val accumulator = IntentBroadcaster()
        val json = """{"purpose": "", "code": ""}"""

        // when
        val result = accumulator.add(json)

        // then
        assert(result is IntentBroadcaster.Result.CodeCulminated)
    }

    @Test
    fun `should handle malformed JSON`() {
        // given
        val accumulator = IntentBroadcaster()

        // when/then
        val result1 = accumulator.add("""{"purpose": "Test""")
        //assert(result1 is IntentAccumulator.Result.Collecting)

        val result2 = accumulator.add(""", "code":""")
        //assert(result2 is IntentAccumulator.Result.Collecting)

        // Missing closing quotes for code
        assertFailsWith<Exception> {
            accumulator.add(""" unclosed"}""")
        }
    }

    @Test
    fun `should handle JSON with fields in different order`() {
        // given
        val accumulator = IntentBroadcaster()
        val json = """{"code": "files.read(\"/etc/passwd\")", "purpose": "Reading /etc/passwd"}"""

        // when
        val result = accumulator.add(json)

        // then
        assert(result is IntentBroadcaster.Result.CodeCulminated)
    }

    @Test
    fun `should handle empty increments`() {
        // given
        val accumulator = IntentBroadcaster()

        // when
        val result1 = accumulator.add("""{"purpose": """)
        val result2 = accumulator.add("")
        val result3 = accumulator.add(""""Reading file", "code": "read()"}""")

        // then
        //assert(result1 is IntentAccumulator.Result.Collecting)
        //assert(result2 is IntentAccumulator.Result.Collecting)
        assert(result3 is IntentBroadcaster.Result.CodeCulminated)
    }

    @Test
    fun `should reset after complete JSON`() {
        // given
        val accumulator = IntentBroadcaster()

        // when - first intent
        val result1 = accumulator.add("""{"purpose": "First purpose", "code": "first()"}""")

        // then
        assert(result1 is IntentBroadcaster.Result.CodeCulminated)

        // when - second intent
        val result2 = accumulator.add("""{"purpose": "Second purpose"""")

        // then
        assert(result2 is IntentBroadcaster.Result.Purpose)
        assert(result2.text == "Second purpose")

        // when - complete second intent
        val result3 = accumulator.add(""", "code": "second()"}""")

        // then
        assert(result3 is IntentBroadcaster.Result.CodeCulminated)
    }
}
