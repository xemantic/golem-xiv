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
import com.xemantic.ai.golem.api.Content
import com.xemantic.ai.golem.api.ContextInfo
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.Message
import com.xemantic.ai.golem.api.Prompt
import com.xemantic.ai.golem.api.ReasoningEvent
import com.xemantic.ai.golem.api.Text
import com.xemantic.ai.golem.server.cognition.cognizer
import com.xemantic.ai.golem.server.script.DefaultWebBrowser
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_API
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_SYSTEM_PROMPT
import com.xemantic.ai.golem.server.script.GolemScriptExecutor
import com.xemantic.ai.golem.server.script.ScriptExecutionException
import com.xemantic.ai.golem.server.script.WebBrowser
import com.xemantic.ai.golem.server.script.extractGolemScript
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.time.Clock
import kotlin.time.Instant
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

    val info: ContextInfo

    suspend fun createMessage(prompt: Prompt): Message

    suspend fun send(message: Message)

}

class Golem(
    private val outputs: FlowCollector<GolemOutput>
) : AutoCloseable {

    private val logger = KotlinLogging.logger {}

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
        private val hasGolemScriptApi: Boolean = true,
        private val creationDate: Instant
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

        override val info: ContextInfo get() = ContextInfo(
            id = id,
            title = "Untitled",
            creationDate = Clock.System.now()
        )

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

        override suspend fun createMessage(
            prompt: Prompt
        ) = Message(
            contextId = id,
            content = prompt.content
        )

        override suspend fun send(message: Message) {
            logger.debug { "Sending message" }
            conversation += message
            scope.launch {
                logger.debug { "Context[$id]: Reasoning" }
                var runGolemScript = false
                do {
                    val cognizer = cognizer() // TODO select based on hints
                    val message = cognizer.reason(
                        golemSystem,
                        conversation,
                        hints = emptyMap()
                    ).fold(MessageAccumulator(contextId = id)) { acc, event ->
                        outputs.emit(GolemOutput.Reasoning(id, acc.messageId, event))
                        acc += event
                        acc
                    }.build()

                    conversation += message

                    val script = extractGolemScript((message.content[0] as Text).text)
                    if (script == null) {
                        runGolemScript = false
                        // TODO sent event that context processing is finished - return control to user
                    } else {
                        println("[machine]> Running Golem Script")
                        println(script)
                        runGolemScript = true
                        val content = try {
                            val scriptResult = scriptExecutor.execute(dependencies, script)
                            if (scriptResult is String) {
                                Text(scriptResult)
                            } else {
                                Text("<golem-script-error>Non-text result</golem-script-error>")
                            }
                        } catch (e: ScriptExecutionException) {
                            Text("<golem-script-error>${e.message}</golem-script-error>")
                        }
                        conversation += Message(contextId = id, content = listOf(content))
                    }
                } while (runGolemScript)
            }
            println("lunched")
        }

    }

    fun newContext(): Context {
        logger.debug { "New context: start" }
        val context = DefaultContext(
            systemPrompt = SYSTEM_PROMPT,
            environmentSystemPrompt = environmentContext(),
            golemScriptApi = GOLEM_SCRIPT_API
        )
        contextMap[context.id] = context
        logger.debug { "New context: created ${context.id}, returning ContextInfo" }
        return context
    }

    fun getContext(id: Uuid): Context? = contextMap[id]

    override fun close() {
        scope.cancel()
    }

}

internal suspend fun FlowCollector<GolemOutput>.emit(
    contextId: Uuid,
    message: Message
) {
    suspend fun emit(event: ReasoningEvent) {
        emit(GolemOutput.Reasoning(contextId, message.id, event))
    }
    emit(ReasoningEvent.MessageStart(role = Message.Role.USER))
    message.content.forEach {
        when (it) {
            is Text -> {
                emit(ReasoningEvent.TextContentStart())
                emit(ReasoningEvent.TextContentDelta(it.text))
                emit(ReasoningEvent.TextContentEnd())
            }
            else -> throw IllegalStateException("Unsupported content type: $it")
        }
    }
    emit(ReasoningEvent.MessageEnd())
}

class MessageAccumulator(
    val messageId: Uuid = Uuid.random(),
    private val contextId: Uuid
) {

    private val textBuilder = StringBuilder()

    private val content = mutableListOf<Content>()

    operator fun plusAssign(event: ReasoningEvent) {
        when (event) {
            is ReasoningEvent.MessageStart -> {}
            is ReasoningEvent.TextContentStart -> {}
            is ReasoningEvent.TextContentDelta -> { textBuilder.append(event.delta) }
            is ReasoningEvent.TextContentEnd -> {
                content += Text(textBuilder.toString())
                textBuilder.clear()
            }
            is ReasoningEvent.MessageEnd -> {}
        }
    }

    fun build() = Message(
        id = messageId,
        contextId = contextId,
        content = content
    )

}
