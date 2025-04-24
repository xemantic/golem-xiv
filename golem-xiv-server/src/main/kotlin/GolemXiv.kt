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
import com.xemantic.ai.golem.server.script.Files
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_API
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_SYSTEM_PROMPT
import com.xemantic.ai.golem.server.script.GolemScript
import com.xemantic.ai.golem.server.script.GolemScriptExecutor
import com.xemantic.ai.golem.server.script.ScriptExecutionException
import com.xemantic.ai.golem.server.script.extractGolemScripts
import com.xemantic.ai.golem.server.script.service.DefaultFiles
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

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
        private val creationDate: Instant = Clock.System.now()
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
            service<Files>("files", DefaultFiles())
//            service<WebBrowser>("browser", DefaultWebBrowser(browser)),
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
            logger.debug { "Context[$id]/Message[${message.id}: Sending" }
            conversation += message
            scope.launch {
                logger.debug { "Context[$id]: Reasoning" }
                var golemScriptResults = emptyList<Any>()
                do {
                    val accumulator = MessageAccumulator(contextId = id)
                    val cognizer = cognizer() // TODO select based on hints
                    golemScriptResults = cognizer.reason(
                        golemSystem,
                        conversation,
                        hints = emptyMap()
                    ).onEach {
                        val reasoning = GolemOutput.Reasoning(
                            contextId = id,
                            messageId = accumulator.messageId,
                            event = it
                        )
                        outputs.emit(reasoning)
                        accumulator += it
                    }.filterIsInstance<ReasoningEvent.TextContentDelta>(
                    ).map {
                        it.delta
                    }.extractGolemScripts(
                    ).transform { script ->
                        runScript(message.id, script)
                    }.toList()

//                    collect {
//                        println("[machine]> Running Golem Script")
//                        println(script)
//                        val content =
//
//                        conversation += Message(contextId = id, content = listOf(content))
//                        if (script == null) {
//                            runGolemScript = false
//                            // TODO sent event that context processing is finished - return control to user
//                        } else {
//
//                        }
//                    }

                    val message = accumulator.build()
                    conversation += message

                } while (golemScriptResults.isNotEmpty())
            }
        }

        private suspend fun FlowCollector<Content>.runScript(
            messageId: Uuid,
            script: GolemScript
        ) {
            logger.debug {
                "Context[$id]/Message[${messageId}: Running GolemScript, purpose: ${script.purpose}, code: ${script.code}"
            }
            try {
                val scriptResult = scriptExecutor.execute(
                    dependencies,
                    script = script.code
                )
                when (scriptResult) {
                    is String -> emit(Text(scriptResult))
                    // TODO add conversion to binary format
                    else -> { logger.error { "Unsupported script result: $scriptResult" }}
                }
            } catch (e: ScriptExecutionException) {
                logger.debug(e) {
                    "Script error"
                }
                emit(Text("<golem-script-error>${e.message}</golem-script-error>"))
            }
        }

    }

    fun newContext(): Context {
        val context = DefaultContext(
            systemPrompt = SYSTEM_PROMPT,
            environmentSystemPrompt = environmentContext(),
            golemScriptApi = GOLEM_SCRIPT_API
        )
        contextMap[context.id] = context
        logger.debug { "Context[${context.id}]: created" }
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
                emit(ReasoningEvent.TextContentStop())
            }
            else -> throw IllegalStateException("Unsupported content type: $it")
        }
    }
    emit(ReasoningEvent.MessageStop())
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
            is ReasoningEvent.TextContentStop -> {
                content += Text(textBuilder.toString())
                textBuilder.clear()
            }
            is ReasoningEvent.MessageStop -> {}
        }
    }

    fun build() = Message(
        id = messageId,
        contextId = contextId,
        content = content
    )

}
