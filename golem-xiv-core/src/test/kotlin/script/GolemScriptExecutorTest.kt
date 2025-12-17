/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.script

import com.xemantic.ai.golem.api.backend.script.ExecuteGolemScript
import com.xemantic.kotlin.test.be
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.sameAs
import com.xemantic.kotlin.test.should
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.measureTimedValue

class GolemScriptExecutorTest {

    @Test
    fun `should execute simple Golem script returning just a String`() = runTest {
        // given
        val script = """
            "foo"
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Value>()
            have(value == "foo")
        }
    }

    @Test
    fun `should execute function defined in the script`() = runTest {
        // given
        val script = """            
            fun passThroughTestFunction(parameter: String) = parameter
            val value = "foo"
            passThroughTestFunction(value)
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Value>()
            have(value == "foo")
        }
    }

    @Test
    fun `should handle script imports`() = runTest {
        // given
        val script = """
            import kotlin.test.Test
            
            @Test
            fun test() = "test function with annotation"

            test()
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Value>()
            have(value == "test function with annotation")
        }
    }

    @Test
    fun `should be able to call a suspended function from within the script`() = runTest {
        // given
        @Suppress("RedundantSuspendModifier")
        val script = """
            suspend fun suspendFunction() = "suspend function result"
            suspendFunction()
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Value>()
            have(value == "suspend function result")
        }
    }

    @Test
    fun `should use dependencies`() = runTest {
        // given
        val script = """
            injected.uppercase()
        """.trimIndent()
        val dependencies = listOf(
            GolemScriptExecutor.Dependency(
                name = "injected",
                type = String::class,
                value = "foo"
            )
        )
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script, dependencies)

        // then
        result should {
            be<ExecuteGolemScript.Result.Value>()
            have(value == "FOO")
        }
    }

    @Test
    fun `should return null`() = runTest {
        // given
        val script = """
            null
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Value>()
            have(value == null)
        }
    }

    @Test
    fun `should return Unit when executing empty script`() = runTest {
        // given
        val script = ""
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Value>()
            have(value is Unit)
        }
    }

    @Test
    fun `should return Unit when executing imports only script`() = runTest {
        // given
        val script = """
            import kotlin.String
            import kotlin.Int
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Value>()
            have(value is Unit)
        }
    }

    @Test
    fun `should return non-returning function call as Unit`() = runTest {
        // given
        val script = """
            println()
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Value>()
            have(value is Unit)
        }
    }

    @Test
    fun `should report multiple syntax errors`() = runTest {
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
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
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

                </golem:impediment>
                
            """.trimIndent()
        }
    }

    @Test
    fun `should report imports error and syntax error`() = runTest {
        // given
        val script = """
            import foo.Bar
            
            buzz
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Unresolved reference 'foo'.
                  at line 1
                  | import <error>foo</error>.Bar

                [ERROR] Unresolved reference 'buzz'.
                  at line 3
                  | <error>buzz</error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report multiline syntax error`() = runTest {
        // given
        val script = """
            val message = ""${'"'}This is a long string
            that spans multiple lines
            and is missing a closing quote
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Expecting '"'
                  at line 3
                  | and is missing a closing quote<error></error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report script runtime exception`() = runTest {
        // given
        val script = """
            throw Exception("must fail")
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="EVALUATION">
                java.lang.Exception: must fail
                  at Script.invokeSuspend(golem-script.kts:1)
                  | throw Exception("must fail")
                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report script exception with the root cause`() = runTest {
        // given
        val script = """
            fun willThrow(value: Int) {
                String::class.java.getDeclaredField("nonExistentField")
            }
            try {
                willThrow(1)
            } catch (e: Exception) {
                throw Exception("must fail", e)
            }
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message.substringBefore("at java.lang.Class.getDeclaredField(Class.java:").trimEnd() sameAs $$"""
                <golem:impediment phase="EVALUATION">
                java.lang.Exception: must fail
                  at Script.invokeSuspend(golem-script.kts:7)
                  |     throw Exception("must fail", e)
                Caused by: java.lang.NoSuchFieldException: nonExistentField
            """.trimIndent()
            message.substringAfter("at java.lang.Class.getDeclaredField(Class.java:").substringAfter("\n") sameAs $$"""
                |  at Script.invokeSuspend$willThrow(golem-script.kts:2)
                |  |     String::class.java.getDeclaredField("nonExistentField")
                |  at Script.invokeSuspend(golem-script.kts:5)
                |  |     willThrow(1)
                |</golem:impediment>
                |
            """.trimMargin()
        }
    }

    /** This test is verifying that executor's main CoroutineScope is a supervising scope. */
    @Test
    fun `should run the second script even if the first one fails`() = runTest {
        // given
        val script1 = """
            throw Exception("must fail")
        """.trimIndent()
        val script2 = """
            "foo"
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result1 = executor.execute(script1)
        val result2 = executor.execute(script2)

        // then
        result1 should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="EVALUATION">
                java.lang.Exception: must fail
                  at Script.invokeSuspend(golem-script.kts:1)
                  | throw Exception("must fail")
                </golem:impediment>

            """.trimIndent()
        }
        result2 should {
            be<ExecuteGolemScript.Result.Value>()
            have(value == "foo")
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `should let the scripts finish first when closing GolemScriptExecutor`() = runTest {
        // given
        val logger = KotlinLogging.logger {}
        val dependencies = listOf(
            GolemScriptExecutor.Dependency(
                name = "logger",
                type = KLogger::class,
                value = logger
            )
        )
        val script = """
            logger.info { "before delay" }
            delay(1000)
            logger.info { "after delay" }
            "foo"
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val deferred1 = async {
            executor.execute(script, dependencies)
        }
        val deferred2 = async {
            executor.execute(script, dependencies)
        }

        advanceUntilIdle()
        executor.close()

        // then
        deferred1.await() should {
            be<ExecuteGolemScript.Result.Value>()
            have(value == "foo")
        }
        deferred2.await() should {
            be<ExecuteGolemScript.Result.Value>()
            have(value == "foo")
        }
    }

    @Test
    fun `should report single unresolved reference error on line 1 without imports`() = runTest {
        // given
        val script = "undefinedVariable"
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Unresolved reference 'undefinedVariable'.
                  at line 1
                  | <error>undefinedVariable</error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report syntax error on single character script`() = runTest {
        // given
        val script = "{"
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Expecting '}'
                  at line 1
                  | {<error></error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report error on script with only whitespace and invalid token`() = runTest {
        // given
        val script = "   @"
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Expected annotation identifier after '@'
                  at line 1
                  |    <error>@</error>

                [ERROR] Expecting an expression
                  at line 1
                  |    @<error></error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report error when script body is just an incomplete expression`() = runTest {
        // given
        val script = "val x ="
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Expecting an expression
                  at line 1
                  | val x =<error></error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report error on script with unmatched closing brace`() = runTest {
        // given
        val script = "}"
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Unexpected symbol
                  at line 1
                  | }<error></error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report error on script with unmatched closing brace after valid code`() = runTest {
        // given
        val script = """
            val x = 1
            }
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Unexpected symbol
                  at line 2
                  | }<error></error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should handle script with multiple closing braces`() = runTest {
        // given
        val script = "}}}"
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Unexpected symbol
                  at line 1
                  | }<error>}</error>}

                [ERROR] Unexpected symbol
                  at line 1
                  | }}<error>}</error>

                [ERROR] Unexpected symbol
                  at line 1
                  | }}}<error></error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report error at end of multiline script`() = runTest {
        // given
        val script = """
            val x = 1
            val y = 2
            undefinedVariable
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Unresolved reference 'undefinedVariable'.
                  at line 3
                  | <error>undefinedVariable</error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report error on mixed opening and closing braces`() = runTest {
        // given
        val script = "{}}"
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Unexpected symbol
                  at line 1
                  | {}}<error></error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report runtime error on line other than 1`() = runTest {
        // given
        val script = """
            val x = 1
            val y = 2
            throw Exception("error on line 3")
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="EVALUATION">
                java.lang.Exception: error on line 3
                  at Script.invokeSuspend(golem-script.kts:3)
                  | throw Exception("error on line 3")
                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should report error with unicode characters`() = runTest {
        // given
        val script = """
            val żółć = "Polish characters"
            val 日本語 = "Japanese characters"
            undefinedUnicode变量
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<ExecuteGolemScript.Result.Error>()
            message sameAs """
                <golem:impediment phase="COMPILATION">
                [ERROR] Unresolved reference 'undefinedUnicode变量'.
                  at line 3
                  | <error>undefinedUnicode变量</error>

                </golem:impediment>

            """.trimIndent()
        }
    }

    @Test
    fun `should execute 100 scripts in a loop`() = runTest {
        // given
        val executor = GolemScriptExecutor()

        // when/then
        repeat(100) { i ->
            val script = $$"""
                val index = $$i
                "result-$index"
            """.trimIndent()

            val result = executor.execute(script)
            result should {
                be<ExecuteGolemScript.Result.Value>()
                have(value == "result-$i")
            }
        }
    }

}
