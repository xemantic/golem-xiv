/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.ai.golem.server.script

import com.xemantic.kotlin.test.assert
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class GolemScriptExecutorTest {

    @Test
    fun `should execute simple Golem script`() = runTest {
        // given
        val script = """
            "foo"
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val result = executor.execute(
            dependencies = emptyList(),
            script = script
        )

        // then
        assert(result == "foo")
    }

    @Test
    fun `should execute own function`() = runTest {
        // given
        val script = """            
            fun myFunction() = "bar"
            myFunction()
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val result = executor.execute(
            dependencies = emptyList(),
            script = script
        )

        // then
        assert(result == "bar")
    }

    @Test
    fun `should use imports`() = runTest {
        // given
        val script = """
            import kotlin.test.Test
            
            @Test
            fun shouldDoTest() = "test function with annotation"

            shouldDoTest()
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val result = executor.execute(
            dependencies = emptyList(),
            script = script
        )

        // then
        assert(result == "test function with annotation")
    }

    @Test
    fun `should use dependencies`() = runTest {
        // given
        val script = """
            injected.length
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val result = executor.execute(
            dependencies = listOf(
                GolemScriptExecutor.Dependency(
                    name = "injected",
                    type = String::class,
                    value = "foo"
                )
            ),
            script = script
        )

        // then
        assert(result == 3)
    }

    @Test
    fun `should return null`() = runTest {
        // given
        val script = """
            null
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val result = executor.execute(
            dependencies = emptyList(),
            script = script
        )

        // then
        assert(result == null)
    }

    @Test
    fun `should return empty Golem script as Unit`() = runTest {
        // given
        val script = ""
        val executor = GolemScriptExecutor(scope = this)

        // when
        val result = executor.execute(
            dependencies = emptyList(),
            script = script
        )

        // then
        assert(result is Unit)
    }

    @Test
    fun `should return non-returning function call as Unit`() = runTest {
        // given
        val script = """
            println()
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val result = executor.execute(
            dependencies = emptyList(),
            script = script
        )

        // then
        assert(result is Unit)
    }

    @Test
    fun `should catch exception on multiple syntax errors`() = runTest {
        // given
        val script = """
            asdf
            println(foo)
            val number: Int = ""${'"'}
                This is a
                multiline string,
                not an Int.
            ""${'"'}; "foo"
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val throwable = assertFails {
            executor.execute(
                dependencies = emptyList(),
                script = script
            )
        }

        // then
        assert(throwable is GolemScriptException)
        val expected = """
            <golem-script> execution failed during phase: COMPILATION
            [ERROR] Unresolved reference 'asdf'.
              at line 1
              | <error>asdf</error>

            [ERROR] Unresolved reference 'foo'.
              at line 2
              | println(<error>foo</error>)

            [ERROR] Initializer type mismatch: expected 'Int', actual 'String'.
              at lines 3-7
              | val number: Int = <error>""${'"'}
              |     This is a
              |     multiline string,
              |     not an Int.
              | ""${'"'}</error>; "foo"


        """.trimIndent()
        assertEquals(actual = throwable.message, expected = expected)
    }

    @Test
    fun `should report wrong imports error and syntax error`() = runTest {
        // given
        val script = """
            import foo.Bar
            
            asdf
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val throwable = assertFails {
            executor.execute(
                dependencies = emptyList(),
                script = script
            )
        }

        // then
        assertEquals(actual = throwable.message, expected = """
            <golem-script> execution failed during phase: COMPILATION
            [ERROR] Wrong import 'foo.Bar'.
              at line 1:
              | import <error>foo.Bar</error>

            [ERROR] Unresolved reference 'asdf'.
              at line 3:
              | println(<error>asdf</error>)


            """.trimIndent()
        )
    }

    @Test
    fun `should catch exception on multiline syntax error with diagnostics`() = runTest {
        // given
        val script = """
            val message = ${"\"\"\""}This is a long string
            that spans multiple lines
            and is missing a closing quote
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val throwable = assertFails {
            executor.execute(
                dependencies = emptyList(),
                script = script
            )
        }

        // then
        assertEquals(throwable.message, """
            <golem-script> execution failed during phase: COMPILATION
            [ERROR] Unresolved reference 'asdf'.
              at line 1:1-1:5
              | asdf

            """.trimIndent()
        )
    }

    @Test
    fun `should catch script exception`() = runTest {
        // given
        val script = /* language=kotlin */ """
            throw Exception("must fail")
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        val throwable = assertFails {
            executor.execute(
                dependencies = emptyList(),
                script = script
            )
        }

        // then
        assertEquals(actual = throwable.message, expected ="""
            <golem-script> execution failed during phase: EVALUATION
            java.lang.Exception: must fail
              at Script.<init>(script.kts:1)
              | throw Exception("must fail")

            """.trimIndent()
        )
    }

    @Test
    fun `should catch script exception with root cause`() = runTest {
        // given
        val script = /* language=kotlin */ """
            fun willThrow() {
                throw RuntimeException("root cause")
            }
            try {
                willThrow()
            } catch (e: RuntimeException) {
                throw Exception("must fail", e)
            }
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        val throwable = assertFails {
            executor.execute(
                dependencies = emptyList(),
                script = script
            )
        }

        // then
        assert(throwable.message == """
            <golem-script> execution failed during phase: EVALUATION
            java.lang.Exception: must fail
              at Script.<init>(script.kts:7)
              |     throw Exception("must fail", e)
            Caused by: java.lang.RuntimeException: root cause
              at Script.willThrow(script.kts:2)
              |     throw RuntimeException("root cause")
              at Script.<init>(script.kts:5)
              |     willThrow()
            
            """.trimIndent()
        )
    }

    @Test
    fun `should not fail on import only script`() = runTest {
        // given
        val script = """
            import kotlin.String
            import kotlin.Int
        """.trimIndent()
        val executor = GolemScriptExecutor(scope = this)

        // when
        val result = executor.execute(
            dependencies = emptyList(),
            script = script
        )

        // then
        assert(result is Unit)
    }

}
