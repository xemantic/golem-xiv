/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core

import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceRepository
import com.xemantic.ai.golem.api.backend.Cognizer
import com.xemantic.ai.golem.api.backend.Identity
import com.xemantic.ai.golem.api.backend.script.ExecuteGolemScript
import com.xemantic.ai.golem.api.backend.script.Files
import com.xemantic.ai.golem.api.backend.script.WebBrowser
import com.xemantic.ai.golem.core.kotlin.getClasspathResource
import com.xemantic.ai.golem.core.script.GolemScriptExecutor
import com.xemantic.ai.golem.core.kotlin.describeCurrentMoment
import com.xemantic.ai.golem.core.os.operatingSystemName
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.plusAssign
import kotlin.time.Clock
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

val golemSystemPrompt = getClasspathResource("/prompts/GolemXIVSystemPrompt.md")

class Golem(
    private val identity: Identity,
    private val repository: CognitiveWorkspaceRepository,
    webBrowserProvider: () -> WebBrowser,
    private val cognizerSelector: (hints: Map<String, String>) -> Cognizer,
    private val files: Files,
    private val outputs: FlowCollector<GolemOutput>
) : AutoCloseable {

    private val logger = KotlinLogging.logger {}

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // In the future it should be some form of distributed marker
    private val activeCognitionMap = ConcurrentHashMap<Long, Job>()

    private val scriptExecutor = GolemScriptExecutor()

    private val selfId = identity.selfId()

    val golemConditioning = buildList {
        //val coreSystem = systemPrompt + if (golemScriptApi != null) GOLEM_SCRIPT_SYSTEM_PROMPT else ""
        val coreSystem = golemSystemPrompt
        add(coreSystem)
        add(environmentContext())
//            if (additionalSystemPrompt != null) {
//                add(System(text = additionalSystemPrompt)) // TODO cache control
//            }
    }

    inner class ActiveCognition(
        private val conditioning: String,
        private val environmentSystemPrompt: String? = null,
        private val golemScriptApi: String? = null,
        private val hasGolemScriptApi: Boolean = true,
    ) {

//        private val workspaceId = repository.initiateWorkspace(
//            conditioning = golemConditioning
//        )

//        override val info: CognitiveWorkspaceInfo get() = CognitiveWorkspaceInfo(
//            id = id,
//            title = "Untitled",
//            creationDate = Clock.System.now()
//        )



//        val kotlinScriptTool = Tool<KotlinScript>(name = "kotlin_script") {
//            //golemScriptExecutor.execute(script)
//        }

//        val golemTools = listOf(kotlinScriptTool)

        val dependencies = listOf(
//            service<com.xemantic.ai.golem.server.script.Context>("phenomena", com.xemantic.ai.golem.server.script.service.DefaultContext(scope, outputs)),
            service<Files>("files", files),
            //service<WebBrowser>("browser", DefaultWebBrowser(browser)),
            service<Memory>("memory", DefaultMemory(neo4j))
////            service<WebBrowserService>("webBrowserService", DefaultWebBrowserService())
////                    service<StringEditorService>("stringEditorService", stringEditorService())
        )

        // TODO it can be done once
        val tool = Tool<ExecuteGolemScript> {
            logger.debug { "Workspace[$id]/GolemScript, purpose: ${this.purpose}" }
            scriptExecutor.execute(script = code)
        }

        val tools = listOf(tool)

        // TODO this should come from the outside
        val human = EpistemicAgent.Human(
            id = 42
        )

        fun structure(
            phenomena: List<Phenomenon>
        ): PhenomenalExpression {

            val info = repository.create(
                conditioning = listOf(systemPrompt) // TODO is it correct?
            )

            return PhenomenalExpression(
                id = info.id,
                agent = EpistemicAgent.Human(
                    id = -2L // TODO where is this id?
                ),
                phenomena = phenomena,
                initiationMoment = info.initiationMoment
            )
        }

        private suspend fun actualize(
            actualizationId: String,
            intent: Phenomenon.Intent,
        ): List<Phenomenon>? {

            logger.debug {
                "Workspace[$id]/Expression[${actualizationId}: " +
                        "Actualizing intent, purpose: ${intent.purpose}, code: ${intent.code}"
            }

            val result = scriptExecutor.execute(
                script = intent.code,
                dependencies = dependencies
            )

            val phenomena = when (result) {
                is ExecuteGolemScript.Result.Value -> when(result.value) {
                    is String -> listOf(
                        Phenomenon.Fulfillment(
                            id = Uuid.random().toString(),
                            intentId = intent.id,
                            intentSystemId = intent.systemId,
                            result = result.value
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
                is ExecuteGolemScript.Result.Error -> listOf(Phenomenon.Impediment(
                    id = Uuid.random().toString(),
                    intentId = intent.id,
                    intentSystemId = intent.systemId,
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
            expression: PhenomenalExpression
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

    suspend fun initiateCognitiveWorkspace(): Long {
        val info = repository.initiateWorkspace(
            conditioning = golemConditioning()
        )
        val cognition = ActiveCognition(
            systemPrompt = golemSystemPrompt,
            environmentSystemPrompt = environmentContext(),
            golemScriptApi = GOLEM_SCRIPT_API
        )
        activeCognitionMap[info.id] = cognition
        logger.debug { "Workspace[${info.id}]: created" }
        return info.id
    }

    suspend fun integrateWithCognitiveWorkspace(
        workspaceId: Long,
        phenomena: List<Phenomenon>
    ) {

        logger.debug { "Workspace[$workspaceId]: Integrating phenomena" }

        suspend fun emit(event: CognitionEvent) {
            outputs.emit(GolemOutput.Cognition(workspaceId, event))
        }

        repository.appendToWorkspace(
            workspaceId = workspaceId,
            agent = EpistemicAgent.Human(
                id = identity.userId("foo") // TODO where to keep this mapping?
            ),
            phenomena = phenomena
        )

        // TODO broadcast
        val workspace = repository.getWorkspace(workspaceId)

//        phenomenalFlow += expression
//
//        emit(expression)

        activeCognitionMap[workspaceId] = scope.launch {

            logger.debug { "Workspace[$workspaceId]: Reasoning" }

            do {

                val cognizer = cognizerSelector(emptyMap()) // TODO select based on hints

                cognizer.reason(
                    conditioning = golemConditioning,
                    workspaceId = workspaceId,
                    workspace.,
                    hints = emptyMap()
                ).collect { event ->
                    emit(event)
                }

                val workspace = repository.getWorkspace(workspaceId)

                val intents = workspace.expressions.last().phenomena.filterIsInstance<Phenomenon.Intent>()

                if (intents.isNotEmpty()) {

                    val initiationMoment = Clock.System.now()

                    val actualizationId = Uuid.random().toString()
                    val agent = EpistemicAgent.Computer(
                        id = -1,
                        belongsToAgentId = -2
                    )

                    emit(
                        CognitionEvent.ExpressionInitiation(
                            expressionId = actualizationId,
                            agent = agent,
                            moment = initiationMoment
                        )
                    )

                    val intent = intents.first()

                    emit(
                        CognitionEvent.FulfillmentInitiation(
                            expressionId = actualizationId,
                        )
                    )

                    // TODO why we need this async?
                    val deferred = scope.async {
                        actualize(actualizationId, intent)
                    }.await()!!

                    emit(
                        CognitionEvent.FulfillmentUnfolding(
                            expressionId = actualizationId,
                            designation = deferred.toString()
                        )
                    )

                    emit(
                        CognitionEvent.FulfillmentCulmination(
                            expressionId = actualizationId
                        )
                    )

                    val culminationMoment = Clock.System.now()

                    val expression = PhenomenalExpression(
                        id = actualizationId,
                        agent = agent,
                        phenomena = deferred,
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

        logger.debug { "Golem XIV closed" }

    }

}
