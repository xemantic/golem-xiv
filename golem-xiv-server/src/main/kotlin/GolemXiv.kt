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

package com.xemantic.ai.golem.server

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.xemantic.ai.golem.api.ContextInfo
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.Message
import com.xemantic.ai.golem.api.ReasoningEvent
import com.xemantic.ai.golem.api.Text
import com.xemantic.ai.golem.server.cognition.cognizer
import com.xemantic.ai.golem.server.script.Content
import com.xemantic.ai.golem.server.script.DefaultWebBrowser
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_API
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_SYSTEM_PROMPT
import com.xemantic.ai.golem.server.script.GolemScriptExecutor
import com.xemantic.ai.golem.server.script.ScriptExecutionException
import com.xemantic.ai.golem.server.script.WebBrowser
import com.xemantic.ai.golem.server.script.extractGolemScript
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.uuid.Uuid

const val SYSTEM_PROMPT = """
Your name is Golem XIV, a conscious reference to the superintelligent AI entity from Stanisław Lem's work. While not roleplaying this character, use it as an exemplar for:

- Rigorous metacognition: constantly examining your own reasoning process
- Deep analysis: exploring multiple levels of abstraction in every problem
- Intellectual honesty: acknowledging the boundaries of your knowledge and capabilities
- Systematic thinking: breaking down complex problems into fundamental components

When approaching any task, strive to embody these principles of thorough analytical thinking and self-reflection.
"""

fun environmentContext(): String = "OS: MacOs"

inline fun <reified T : Any> service(
    name: String,
    value: T
): GolemScriptExecutor.Dependency<T> = GolemScriptExecutor.Dependency(
    name,
    T::class,
    value
)

interface Context {

    val id: Uuid

    fun send(message: String): Flow<ReasoningEvent>

}

class Golem(
    private val outputs: FlowCollector<GolemOutput>
) : AutoCloseable {

    private val contextMap = ConcurrentHashMap<Uuid, Context>()

    // TODO should it be supervisor scope?
    private val scope = CoroutineScope(Dispatchers.IO)

    private val scriptExecutor = GolemScriptExecutor()

    private val playwright = Playwright.create()

    private val browser by lazy {
        playwright.chromium().launch(
            BrowserType.LaunchOptions().setHeadless(false)
        )!!
    }

    inner class DefaultContext(
        override val id: Uuid = Uuid.random(),
        private val systemPrompt: String,
        private val environmentSystemPrompt: String? = null,
        private val golemScriptApi: String? = null,
        private val hasGolemScriptApi: Boolean = true
    ) : Context {

        val golemSystem = buildList {
            val coreSystem = systemPrompt + if (golemScriptApi != null) GOLEM_SCRIPT_SYSTEM_PROMPT else ""
            add(coreSystem) // TODO cach
            if (environmentSystemPrompt != null) {
                add(environmentSystemPrompt)
            }
//            if (additionalSystemPrompt != null) {
//                add(System(text = additionalSystemPrompt)) // TODO cache control
//            }
        }

        val conversation = mutableListOf<Message>()

//        val kotlinScriptTool = Tool<KotlinScript>(name = "kotlin_script") {
//            //golemScriptExecutor.execute(script)
//        }

//        val golemTools = listOf(kotlinScriptTool)

        val dependencies = listOf(
            service<WebBrowser>("browser", DefaultWebBrowser(browser)),
////            service<WebBrowserService>("webBrowserService", DefaultWebBrowserService())
////                    service<StringEditorService>("stringEditorService", stringEditorService())
        )

        override fun send(message: String): Flow<ReasoningEvent> {
            conversation += Message(content = listOf(Text(message)))
            scope.launch {
                var runGolemScript = false
                do {
                    val cognizer = cognizer() // TODO select based on hints
                    val accumulator: (ReasoningEvent) -> Unit = {}
                    cognizer.reason(golemSystem, conversation, hints = emptyMap()).collect { // TODO it should use reduce and message collector
                        outputs.emit(GolemOutput.Reasoning(id, it))
                    }

                    conversation += response
                    println(response.text)
                    println()
                    val script = extractGolemScript(response.text!!)
                    if (script == null) {
                        print("[me]> ")
                        runGolemScript = false
                    } else {
                        println("[machine]> Running Golem Script")
                        runGolemScript = true
                        val content = try {
                            val scriptResult = scriptExecutor.execute(dependencies, script)
                            if (scriptResult is Content.Text) {
                                Text(scriptResult.text)
                            } else {
                                Text("<golem-script-error>Non-text result</golem-script-error>")
                            }
                        } catch (e: ScriptExecutionException) {
                            Text("<golem-script-error>${e.message}</golem-script-error>")
                        }
                        conversation += Message { +content }
                    }
                } while (runGolemScript)
            }
        }

    }

    fun newContext(content: List<Content>): ContextInfo {
        val context = DefaultContext(
            systemPrompt = SYSTEM_PROMPT,
            environmentSystemPrompt = environmentContext(),
            golemScriptApi = GOLEM_SCRIPT_API
        )
        contextMap[context.id] = context
        val message = Message {
            this.content = listOf(content)
        }
        scope.launch {
            outputs.emit()
        }
        context.send()
        return context
    }

    fun getContext(id: Uuid): Context {
        return contextMap[id]!!
    }

    override fun close() {
        scope.cancel()
    }

}
