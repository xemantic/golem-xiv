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

import com.xemantic.kotlin.test.be
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GolemScriptExecutorTest {

    @Test
    fun `should execute simple Golem script returning just a String`() = runTest {
        // given
        val script = /* language=kotlin */ """
            "foo"
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<GolemScript.Result.Value>()
            have(value == "foo")
        }
    }

    @Test
    fun `should execute function defined in the script`() = runTest {
        // given
        val script = /* language=kotlin */ """            
            fun passThroughTestFunction(parameter: String) = parameter
            val value = "foo"
            passThroughTestFunction(value)
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<GolemScript.Result.Value>()
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
            be<GolemScript.Result.Value>()
            have(value == "test function with annotation")
        }
    }

    @Test
    fun `should be able to call a suspended function from within the script`() = runTest {
        // given
        @Suppress("RedundantSuspendModifier")
        val script = /* language=kotlin */ """
            suspend fun suspendFunction() = "suspend function result"
            suspendFunction()
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<GolemScript.Result.Value>()
            have(value == "suspend function result")
        }
    }

    @Test
    fun `should use dependencies`() = runTest {
        // given
        val script = /* language=kotlin */ """
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
            be<GolemScript.Result.Value>()
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
            be<GolemScript.Result.Value>()
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
            be<GolemScript.Result.Value>()
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
            be<GolemScript.Result.Value>()
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
            be<GolemScript.Result.Value>()
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
            be<GolemScript.Result.Error>()
            have(message == """
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
            )
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
            be<GolemScript.Result.Error>()
            have(message == """
                <golem-script> execution failed during phase: COMPILATION
                [ERROR] Unresolved reference 'foo'.
                  at line 1
                  | import <error>foo</error>.Bar
    
                [ERROR] Unresolved reference 'buzz'.
                  at line 3
                  | <error>buzz</error>
    
    
                """.trimIndent()
            )
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
            be<GolemScript.Result.Error>()
            have(message == """
                <golem-script> execution failed during phase: COMPILATION
                [ERROR] Expecting '"'
                  at line 3
                  | and is missing a closing quote<error/>
    
    
                """.trimIndent()
            )
        }
    }

    @Test
    fun `should report script runtime exception`() = runTest {
        // given
        val script = /* language=kotlin */ """
            throw Exception("must fail")
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result = executor.execute(script)

        // then
        result should {
            be<GolemScript.Result.Error>()
            have(message == """
                <golem-script> execution failed during phase: EVALUATION
                java.lang.Exception: must fail
                  at Script.invokeSuspend(golem-script.kts:1)
                  | throw Exception("must fail")
    
    
                """.trimIndent()
            )
        }
    }

    @Test
    fun `should report script exception with the root cause`() = runTest {
        // given
        val script = /* language=kotlin */ """
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
            be<GolemScript.Result.Error>()
            have(message == $$"""
                <golem-script> execution failed during phase: EVALUATION
                java.lang.Exception: must fail
                  at Script.invokeSuspend(golem-script.kts:7)
                  |     throw Exception("must fail", e)
                Caused by: java.lang.NoSuchFieldException: nonExistentField
                  at java.lang.Class.getDeclaredField(Class.java:2841)
                  at Script.invokeSuspend$willThrow(golem-script.kts:2)
                  |     String::class.java.getDeclaredField("nonExistentField")
                  at Script.invokeSuspend(golem-script.kts:5)
                  |     willThrow(1)


                """.trimIndent()
            )
        }
    }

    /** This test is verifying that executor's main CoroutineScope is a supervising scope. */
    @Test
    fun `should run the second script even if the first one fails`() = runTest {
        // given
        val script1 = /* language=kotlin */ """
            throw Exception("must fail")
        """.trimIndent()
        val script2 = /* language=kotlin */ """
            "foo"
        """.trimIndent()
        val executor = GolemScriptExecutor()

        // when
        val result1 = executor.execute(script1)
        val result2 = executor.execute(script2)

        // then
        result1 should {
            be<GolemScript.Result.Error>()
            have(message == """
                <golem-script> execution failed during phase: EVALUATION
                java.lang.Exception: must fail
                  at Script.invokeSuspend(golem-script.kts:1)
                  | throw Exception("must fail")


                """.trimIndent()
            )
        }
        result2 should {
            be<GolemScript.Result.Value>()
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
        val script = /* language=kotlin */ """
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
            be<GolemScript.Result.Value>()
            have(value == "foo")
        }
        deferred2.await() should {
            be<GolemScript.Result.Value>()
            have(value == "foo")
        }
    }

}
