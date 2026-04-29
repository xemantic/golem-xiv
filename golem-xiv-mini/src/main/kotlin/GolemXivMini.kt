/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.golem.mini

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.anthropic.cache.CacheControl
import com.xemantic.ai.anthropic.event.Event
import com.xemantic.ai.anthropic.message.*
import com.xemantic.ai.golem.api.backend.script.ExecuteGolemScript
import com.xemantic.ai.golem.core.script.GolemScriptExecutor
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

val constitution = """
You can execute Kotlin Script by wrapping it inside <golem:script> tags.
The script execution environment is already providing a CoroutineScope, therefore suspending functions can be called directly.
The last expression of the script can be of `Any?` type, and will be returned to you after `toString()` conversion.
If compilation/evaluation errors occur, they will be sent back to you instead wrapped in <golem:impediment> tag.

You can initiate a recursive cogitation which inherits this system prompt:

```kotlin
val result: String = golem.cogitate("Level 1: Initial prompt of recursive cogitation")
```

The root cogitation has level 0, therefore each recursive cogitation should have +1 increased level indicated at the beginning of the initial prompt.

Each cogitation, including root cogitation, is an agent loop which can be exited with:
 
```kotlin
golem.respond("foo")
```

placed as the last expression in the script.

When working on complex problems, always consider breaking them down into recursive cogitations.
""".trimIndent()

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Usage: golem-xiv-mini [input can consist of multiple words]")
        exitProcess(2)
    }

    val input = args.joinToString(separator = " ")

    val golem = MiniGolem()

    runBlocking {
        golem.cogitate(input)
    }
}

class MiniGolem {

    val dependencies = listOf(
        GolemScriptExecutor.Dependency(
            name = "golem",
            type = MiniGolem::class,
            value = this@MiniGolem
        )
    )

    val scriptExecutor = GolemScriptExecutor()
    val anthropic = Anthropic()

    suspend fun cogitate(input: String): String {

        println("___Loop_____________________:\n")
        println(input)
        println()

        val cogitation = mutableListOf<Message>()
        cogitation += input

        var result: Any? = null
        do {

            println("___Intent___________________:\n")

            val llmResponse = anthropic.messages.stream {
                messages = cogitation.addCacheBreakpoint()
                system = listOf(System(
                    text = constitution,
                    cacheControl = CacheControl.Ephemeral {
                        ttl = CacheControl.Ephemeral.TTL.ONE_HOUR
                }))
                stopSequences = listOf("</golem:script>")
            }.onEach { event ->
                if (event is Event.ContentBlockDelta) {
                    val delta = event.delta
                    if (delta is Event.ContentBlockDelta.Delta.TextDelta) {
                        print(delta.text)
                    }
                }
            }.toMessageResponse()
            cogitation += llmResponse

            val responseText = llmResponse.text!!
            val hasScript = responseText.contains("<golem:script>")
            if (hasScript) {
                println("</golem:script>")
            }
            println()
            if (hasScript) {
                val code = responseText.substringAfter("<golem:script>")
                result = when (val scriptResult = scriptExecutor.execute(code, dependencies)) {
                    is ExecuteGolemScript.Result.Error -> scriptResult.message
                    is ExecuteGolemScript.Result.Value -> scriptResult.value
                }
                val strResult = result.toString()
                cogitation += strResult
                println("___Fulfillment______________:\n")
                println(strResult)
            }
        } while (hasScript && result !is CogitationResult)
        println("\n")
        return result.toString()
    }

    fun respond(message: String) = CogitationResult(message)

}

class CogitationResult(val text: String) {
    override fun toString(): String = text
}
