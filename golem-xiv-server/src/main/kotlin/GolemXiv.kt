/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.xemantic.ai.anthropic.tool.Tool
import com.xemantic.ai.golem.api.Agent
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.Expression
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.server.cognition.cognizer
import com.xemantic.ai.golem.server.kotlin.describeCurrentMoment
import com.xemantic.ai.golem.server.neo4j.Neo4JProvider
import com.xemantic.ai.golem.server.os.operatingSystemName
import com.xemantic.ai.golem.server.phenomena.ExpressionAccumulator
import com.xemantic.ai.golem.server.script.Files
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_API
import com.xemantic.ai.golem.server.script.GOLEM_SCRIPT_SYSTEM_PROMPT
import com.xemantic.ai.golem.server.script.GolemScript
import com.xemantic.ai.golem.server.script.GolemScriptExecutor
import com.xemantic.ai.golem.server.script.Memory
import com.xemantic.ai.golem.server.script.WebBrowser
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
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import java.util.concurrent.ConcurrentHashMap
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

interface CognitiveWorkspace {

    val id: String

    suspend fun structure(
        phenomena: List<Phenomenon>
    ): Expression

    suspend fun integrate(
        expression: Expression
    )

}

class Golem(
    private val outputs: FlowCollector<GolemOutput>
) : AutoCloseable {

    private val logger = KotlinLogging.logger {}

    private val workspaceMap = ConcurrentHashMap<String, CognitiveWorkspace>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val scriptExecutor = GolemScriptExecutor()

    private val neo4JProvider = Neo4JProvider()

    private val playwright = Playwright.create()

    private val browser by lazy {
        playwright.chromium().launch(
            BrowserType.LaunchOptions().setHeadless(false)
        )!!
    }

    val neo4j = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "neo4jneo4j"))

    inner class DefaultCognitiveWorkspace(
        override val id: String = Uuid.random().toString(),
        private val systemPrompt: String,
        private val environmentSystemPrompt: String? = null,
        private val golemScriptApi: String? = null,
        private val hasGolemScriptApi: Boolean = true,
        private val creationDate: Instant = Clock.System.now()
    ) : CognitiveWorkspace {

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

//        override val info: CognitiveWorkspaceInfo get() = CognitiveWorkspaceInfo(
//            id = id,
//            title = "Untitled",
//            creationDate = Clock.System.now()
//        )

        val phenomenalFlow = mutableListOf<Expression>()

//        val kotlinScriptTool = Tool<KotlinScript>(name = "kotlin_script") {
//            //golemScriptExecutor.execute(script)
//        }

//        val golemTools = listOf(kotlinScriptTool)



        val dependencies = listOf(
            service<com.xemantic.ai.golem.server.script.Context>("phenomena", com.xemantic.ai.golem.server.script.service.DefaultContext(scope, outputs)),
            service<Files>("files", DefaultFiles()),
            service<WebBrowser>("browser", DefaultWebBrowser(browser)),
            service<Memory>("memory", DefaultMemory(neo4j))
////            service<WebBrowserService>("webBrowserService", DefaultWebBrowserService())
////                    service<StringEditorService>("stringEditorService", stringEditorService())
        )

        val tool = Tool<GolemScript> {
            logger.debug { "Context[$id]/GolemScript, purpose: ${this.purpose}" }
            scriptExecutor.execute(script = code)
        }

        val tools = listOf(tool)

        val userAgent = Agent(
            id = "user",
            description = "The user",
            category = Agent.Category.HUMAN
        )

        val agent = Agent(
            id = "golem",
            description = "The agent",
            category = Agent.Category.SELF,
            model = "N/A", // TODO should be provided by cognizer
            vendor = "N/A"
        )

        override suspend fun structure(
            phenomena: List<Phenomenon>
        ): Expression {
            val now = Clock.System.now()
            return Expression(
                id = Uuid.random().toString(),
                agent = userAgent,
                phenomena = phenomena,
                initiationMoment = now,
                culminationMoment = now
            )
        }

        override suspend fun integrate(
            expression: Expression
        ) {
            logger.debug { "Workspace[$id]/Expression[${expression.id}: Integrating" }
            phenomenalFlow += expression

            emit(expression)

            scope.launch {
                logger.debug { "Workspace[$id]: Reasoning" }

                do {

                    val accumulator = ExpressionAccumulator(workspaceId = id)

                    val cognizer = cognizer(tools) // TODO select based on hints

                    cognizer.reason(
                        golemSystem,
                        phenomenalFlow,
                        hints = emptyMap()
                    ).collect { event ->
                        accumulator += event
                        emit(event) // TODO don't output script events, just the script - fix the intent purpose scanning
                    }

                    val expression = accumulator.build()

                    phenomenalFlow += expression

                    val intents = expression.phenomena.filterIsInstance<Phenomenon.Intent>()

                    if (intents.isNotEmpty()) {

                        val initiationMoment = Clock.System.now()

                        val actualizationId = Uuid.random().toString()
                        val agent = Agent(
                            id = "computer",
                            description = "user's computer",
                            category = Agent.Category.OTHER_MACHINE
                        )

                        emit(
                            CognitionEvent.ExpressionInitiation(
                                expressionId = actualizationId,
                                agent = agent,
                                moment = initiationMoment
                            )
                        )

                        intents.forEach { intent ->

                            emit(
                                CognitionEvent.FulfillmentInitiation(
                                    expressionId = actualizationId,
                                )
                            )

                            // TODO why we need this async?
                            val deferred = scope.async {
                                actualize(actualizationId, intent)
                            }

                            val result = deferred.await()

                            emit(
                                CognitionEvent.FulfillmentCulmination(
                                    expressionId = actualizationId,
                                )
                            )

                        }

                        val culminationMoment = Clock.System.now()

                        val expression = Expression(
                            id = actualizationId,
                            agent = agent,
                            phenomena = emptyList(),
                            initiationMoment = initiationMoment,
                            culminationMoment = culminationMoment
                        )

                        phenomenalFlow += expression

                        emit(
                            CognitionEvent.ExpressionCulmination(
                                expressionId = actualizationId,
                                moment = culminationMoment
                            )
                        )

                    }

                } while (intents.isNotEmpty())
            }
        }



        private suspend fun actualize(
            actualizationId: String,
            intent: Phenomenon.Intent,
        ): List<Phenomenon>? {

            logger.debug {
                "Workspace[$id]/Expression[${actualizationId}: " +
                        "Actualizing intent, purpose: ${intent.purpose}, instructions: ${intent.instructions}"
            }

            val result = scriptExecutor.execute(
                script = intent.instructions,
                dependencies = dependencies
            )

            val phenomena = when (result) {
                is GolemScript.Result.Value -> when(result.value) {
                    is String -> listOf(
                        Phenomenon.Text(
                            id = Uuid.random().toString(),
                            text = result.value
                        )
                    )
                    is Unit -> null
                    else -> listOf(
                        Phenomenon.Text(
                            id = Uuid.random().toString(),
                            text = result.value.toString()
                        )
                    )
                }
                is GolemScript.Result.Error -> listOf(Phenomenon.Impediment(
                    id = Uuid.random().toString(),
                    intentId = intent.id,
                    reason = result.message
                ))
            }

            return phenomena
        }

        private suspend fun emit(event: CognitionEvent) {
            outputs.emit(GolemOutput.Cognition(workspaceId = id, event))
        }

        // used to send back initial expression via websocket
        internal suspend fun emit(
            expression: Expression
        ) {
            emit(
                CognitionEvent.ExpressionInitiation(
                    expressionId = expression.id,
                    agent = userAgent,
                    expression.initiationMoment
                )
            )
            expression.phenomena.forEach {
                when (it) {
                    is Phenomenon.Text -> {
                        emit(CognitionEvent.TextInitiation(expression.id))
                        emit(CognitionEvent.TextUnfolding(expression.id, it.text))
                        emit(CognitionEvent.TextCulmination(expression.id))
                    }
                    else -> throw IllegalStateException("Unsupported content type: $it")
                }
            }
            emit(
                CognitionEvent.ExpressionCulmination(
                    expressionId = expression.id,
                    moment = expression.culminationMoment!!,
                )
            )
        }

    }

    fun newCognitiveWorkspace(): CognitiveWorkspace {
        val context = DefaultCognitiveWorkspace(
            systemPrompt = SYSTEM_PROMPT,
            environmentSystemPrompt = environmentContext(),
            golemScriptApi = GOLEM_SCRIPT_API
        )
        workspaceMap[context.id] = context
        logger.debug { "Context[${context.id}]: created" }
        return context
    }

    fun getCognitiveWorkspace(id: String): CognitiveWorkspace? = workspaceMap[id]

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

        neo4j.close()
        //neo4JProvider.close()

        logger.debug { "Golem XIV closed" }

    }

}
