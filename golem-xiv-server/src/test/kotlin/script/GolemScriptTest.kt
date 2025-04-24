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
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GolemScriptTest {

    @Test
    fun `should extract simple script tag`() = runTest {
        // given
        val flow = flowOf("<golem-script purpose=\"test\">Test content</golem-script>")

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "test")
                have(code == "Test content")
            }
        }
    }

    @Test
    fun `should extract multiline script tag`() = runTest {
        // given
        val flow = flowOf("""
            <golem-script purpose="test">
            Test content
            </golem-script>
        """".trimIndent())

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "test")
                have(code == "Test content")
            }
        }
    }

    @Test
    fun `should extract multiple script tags`() = runTest {
        // given
        val flow = flowOf(
            "<golem-script purpose=\"test1\">Test content 1</golem-script>" +
                    "<golem-script purpose=\"test2\">Test content 2</golem-script>"
        )

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 2)
            this[0] should {
                have(purpose == "test1")
                have(code == "Test content 1")
            }
            this[1] should {
                have(purpose == "test2")
                have(code == "Test content 2")
            }
        }
    }

    @Test
    fun `should extract script tag across emissions`() = runTest {
        // given
        val flow = flowOf(
            "<golem-script purpose=\"test\">",
            "Test content",
            "</golem-script>"
        )

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "test")
                have(code == "Test content")
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
            <golem-script purpose=\"code\">
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

    // TODO what should happen with empty purpose
    @Test
    fun `should not extract script with empty purpose`() = runTest {
        val flow = flowOf("<golem-script purpose=\"\">Test content</golem-script>")
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "")
                have(code == "Test content")
            }
        }
    }

    @Test
    fun `should extract script with multiline content`() = runTest {
        // given
        val flow = flowOf("""
            <golem-script purpose="multiline">
            Line 1
            Line 2
            Line 3
            </golem-script>
        """.trimIndent())

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "multiline")
                have(code == "\nLine 1\nLine 2\nLine 3")
            }
        }
    }

    @Test
    fun `extract script tag with fragmented chunks`() = runTest {
        // given
        val flow = flowOf(
            "<golem-script pur",
            "pose=\"fragmented\">Content ",
            "split across ",
            "multiple chunks</golem-",
            "script>"
        )

        // when
        val scripts = flow.extractGolemScripts().toList()

        // then
        scripts should {
            have(size == 1)
            this[0] should {
                have(purpose == "fragmented")
                have(code == "Content split across multiple chunks")
            }
        }
    }

}
