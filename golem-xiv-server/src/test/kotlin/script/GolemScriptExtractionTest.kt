/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GolemScriptExtractionTest {

    @Test
    fun `should extract simple script tag`() = runTest {
        // given
        val flow = flowOf("""<golem-script purpose="test">"foo"</golem-script>""")

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "test")
                have(code == "\"foo\"")
            }
        }
    }

    @Test
    fun `should extract multiline script tag`() = runTest {
        // given
        val flow = flowOf("""
            <golem-script purpose="test">
            println("foo")
            </golem-script>
        """".trimIndent())

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "test")
                have(code == "println(\"foo\")")
            }
        }
    }

    @Test
    fun `should extract multiple multiline script tags from chunked String flow`() = runTest {
        // given
        val input = """
            <golem-script purpose="Set title">
            context.title = "Test run"
            </golem-script>
            
            Some non-script paragraph.
            
            <golem-script purpose="Calculate numbers">
            val result = 2+2
            println(result)
            result
            </golem-script>
        """.trimIndent()

        val flow = input.chunked(5).asFlow()

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 2)
            this[0] should {
                have(purpose == "Set title")
                have(code == "context.title = \"Test run\"")
            }
            this[1] should {
                have(purpose == "Calculate numbers")
                have(code == """
                    val result = 2+2
                    println(result)
                    result
                """.trimIndent())
            }
        }
    }

    @Test
    fun `should not extract script from non-script content`() = runTest {
        // given
        val flow = flowOf("No script tags here")

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        assert(scripts.isEmpty())
    }

    @Test
    fun `should not extract incomplete script tag`() = runTest {
        // given
        val flow = flowOf("<golem-script purpose=\"test\">Test content")

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        assert(scripts.isEmpty())
    }

    @Test
    fun `should extract script with complex content`() = runTest {
        // given
        val script = /* language=kotlin */ """
            fun test() {
                return "This is a test"
            }
        """.trimIndent()

        val flow = flowOf("""
            <golem-script purpose="code">
            $script
            </golem-script>"
        """.trimIndent())

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "code")
                have(code == script)
            }
        }
    }

    @Test
    fun `should extract script with empty purpose`() = runTest {
        val flow = flowOf("<golem-script purpose=\"\">null</golem-script>")
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "")
                have(code == "null")
            }
        }
    }

}
