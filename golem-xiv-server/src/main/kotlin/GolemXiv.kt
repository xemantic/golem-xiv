/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
import com.xemantic.ai.golem.server.kotlin.awaitEach
import com.xemantic.ai.golem.server.kotlin.describeCurrentMoment
import com.xemantic.ai.golem.server.neo4j.Neo4JProvider
import com.xemantic.ai.golem.server.os.operatingSystemName
import com.xemantic.ai.golem.server.script.Files
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_API
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_SYSTEM_PROMPT
import com.xemantic.ai.golem.server.script.GolemScript
import com.xemantic.ai.golem.server.script.GolemScriptExecutor
import com.xemantic.ai.golem.server.script.Memory
import com.xemantic.ai.golem.server.script.WebBrowser
import com.xemantic.ai.golem.server.script.extractGolemScripts
import com.xemantic.ai.golem.server.script.service.DefaultFiles
import com.xemantic.ai.golem.server.script.service.DefaultMemory
import com.xemantic.ai.golem.server.script.service.DefaultWebBrowser
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun environmentContext(): String = """
Host OS: ${operatingSystemName()}
Current time: ${describeCurrentMoment()}
""".trimIndent()

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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val scriptExecutor = GolemScriptExecutor()

    private val neo4JProvider = Neo4JProvider()

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
            service<com.xemantic.ai.golem.server.script.Context>("context", com.xemantic.ai.golem.server.script.service.DefaultContext(scope, outputs)),
            service<Files>("files", DefaultFiles()),
            service<WebBrowser>("browser", DefaultWebBrowser(browser)),
            //service<Memory>("memory", DefaultMemory(neo4JProvider.graphDb))
////            service<WebBrowserService>("webBrowserService", DefaultWebBrowserService())
////                    service<StringEditorService>("stringEditorService", stringEditorService())
        )

        override suspend fun createMessage(
            prompt: Prompt
        ) = Message(
            id = Uuid.random(),
            contextId = id,
            content = prompt.content
        )

        override suspend fun send(message: Message) {
            logger.debug { "Context[$id]/Message[${message.id}: Sending" }
            conversation += message
            scope.launch {
                logger.debug { "Context[$id]: Reasoning" }

                do {

                    val accumulator = MessageAccumulator(contextId = id)

                    var responseMessageId: Uuid? = null

                    val cognizer = cognizer() // TODO select based on hints
                    val golemScriptResults = cognizer.reason(
                        golemSystem,
                        conversation,
                        hints = emptyMap()
                    ).onEach {
                        val reasoning = GolemOutput.Reasoning(
                            contextId = id,
                            event = it
                        )
                        accumulator += it
                        outputs.emit(reasoning)
                    }.filterIsInstance<ReasoningEvent.TextContentDelta>(
                    ).map {
                        it.delta
                    }.extractGolemScripts(
                    ).map { script ->
                        scope.async {
                            runScript(message.id, script)
                        }
                    }.toList().awaitEach {

                        if (responseMessageId == null) {
                            responseMessageId = Uuid.random()
                            outputs.emitReasoningEvent(
                                ReasoningEvent.MessageStart(
                                    messageId = responseMessageId,
                                    role = Message.Role.USER
                                )
                            )
                        }

                        it?.forEach {
                            outputs.emitReasoningEvent(
                                ReasoningEvent.TextContentStart(responseMessageId)
                            )
                            outputs.emitReasoningEvent(
                                ReasoningEvent.TextContentDelta(responseMessageId, (it as Text).text)
                            )
                            outputs.emitReasoningEvent(
                                ReasoningEvent.TextContentStop(responseMessageId)
                            )
                        }
                    }.filterNotNull().flatten()

                    val message = accumulator.build()

                    if (responseMessageId != null) {
                        outputs.emitReasoningEvent(
                            ReasoningEvent.MessageStop(responseMessageId)
                        )
                    }

                    conversation += message

                    if (responseMessageId != null) {
                        conversation += Message(
                            id = responseMessageId,
                            contextId = id,
                            content = golemScriptResults
                        )
                    }

                } while (golemScriptResults.isNotEmpty())
            }
        }

        private suspend fun runScript(
            messageId: Uuid,
            script: GolemScript
        ): List<Content>? {
            logger.debug {
                "Context[$id]/Message[${messageId}: Running GolemScript, purpose: ${script.purpose}, code: ${script.code}"
            }
            val result = scriptExecutor.execute(
                script = script.code,
                dependencies = dependencies
            )
            val content = when (result) {
                is GolemScript.Result.Value -> when(result.value) {
                    is String -> listOf(Text(result.value))
                    is Unit -> null
                    else -> listOf(Text(result.value.toString()))
                }
                is GolemScript.Result.Error -> listOf(Text(
                    "<golem-script-error>${result.message}</golem-script-error>"
                ))
            }
            return content
        }

        private suspend fun FlowCollector<GolemOutput>.emitReasoningEvent(
            event: ReasoningEvent
        ) {
            emit(GolemOutput.Reasoning(contextId = id, event))
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

        logger.debug { "Closing Golem XIV" }

        runBlocking {
            scope.coroutineContext.job.children.forEach {
                it.join()
            }
        }

        scriptExecutor.close()

        scope.cancel()

        runBlocking {
            scope.coroutineContext.job.join()
        }

        neo4JProvider.close()

        logger.debug { "Golem XIV closed" }

    }

}

internal suspend fun FlowCollector<GolemOutput>.emit(
    contextId: Uuid,
    message: Message
) {
    suspend fun emit(event: ReasoningEvent) {
        emit(GolemOutput.Reasoning(contextId, event))
    }
    emit(ReasoningEvent.MessageStart(message.id, role = Message.Role.USER))
    message.content.forEach {
        when (it) {
            is Text -> {
                emit(ReasoningEvent.TextContentStart(message.id))
                emit(ReasoningEvent.TextContentDelta(message.id, it.text))
                emit(ReasoningEvent.TextContentStop(message.id))
            }
            else -> throw IllegalStateException("Unsupported content type: $it")
        }
    }
    emit(ReasoningEvent.MessageStop(message.id))
}

class MessageAccumulator(
    private val contextId: Uuid
) {

    private val textBuilder = StringBuilder()

    private val content = mutableListOf<Content>()

    private var id: Uuid? = null

    operator fun plusAssign(event: ReasoningEvent) {
        when (event) {
            is ReasoningEvent.MessageStart -> id = event.messageId
            is ReasoningEvent.TextContentStart -> {}
            is ReasoningEvent.TextContentDelta -> { textBuilder.append(event.delta) }
            is ReasoningEvent.TextContentStop -> {
                content += Text(textBuilder.toString())
                textBuilder.clear()
            }
            is ReasoningEvent.MessageStop -> {}
            else -> {}
        }
    }

    fun build() = Message(
        id = id!!,
        contextId = contextId,
        content = content
    )

}
