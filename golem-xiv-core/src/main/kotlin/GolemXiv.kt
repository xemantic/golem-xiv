/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core

import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.backend.CognitiveWorkspaceRepository
import com.xemantic.ai.golem.api.backend.Cognizer
import com.xemantic.ai.golem.api.backend.AgentIdentity
import com.xemantic.ai.golem.api.backend.script.ExecuteGolemScript
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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

fun environmentalContext(): String = """
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

val golemMainConditioning = getClasspathResource("/conditioning/GolemXIVConditioning.md")

class Golem(
    private val identity: AgentIdentity,
    private val repository: CognitiveWorkspaceRepository,
    private val cognizer: Cognizer,
    private val golemScriptDependencies: List<GolemScriptExecutor.Dependency<*>>,
    private val outputs: FlowCollector<GolemOutput>
) : AutoCloseable {

    private val logger = KotlinLogging.logger {}

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // In the future it should be some form of distributed marker
    private val activeCognitionMap = ConcurrentHashMap<Long, Job>()

    private val scriptExecutor = GolemScriptExecutor()

    val golemConditioning = buildList {
        //val coreSystem = systemPrompt + if (golemScriptApi != null) GOLEM_SCRIPT_SYSTEM_PROMPT else ""
        add(golemMainConditioning)
        add(environmentalContext())
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

        // TODO this should come from the outside
//        val human = EpistemicAgent.Human(
//            id = 42
//        )



    }

    suspend fun initiateCognition(): Long {
        val info = repository.initiateWorkspace(
            conditioning = golemConditioning
        )
        return info.id
    }

    /**
     * @throws com.xemantic.ai.golem.api.backend.GolemException
     */
    suspend fun perceive(
        workspaceId: Long,
        phenomena: List<Phenomenon>
    ) {

        // TODO check if cognition is active
        logger.debug { "Workspace[$workspaceId]: perceiving phenomena" }

        val phenomenalExpression = repository.appendToWorkspace(
            workspaceId = workspaceId,
            agent = EpistemicAgent.Human(
                id = identity.getUserId("foo") // TODO where to keep this mapping?
            ),
            phenomena = phenomena
        )

        val cognitionBroadcaster = outputs.cognitionBroadcaster(workspaceId)

        cognitionBroadcaster.emit(phenomenalExpression)

        activeCognitionMap[workspaceId] = scope.launch {

            logger.debug { "Workspace[$workspaceId]: Initiating cognition" }

            do {

                val workspace = repository.getWorkspace(workspaceId)

                cognizer.reason(
                    conditioning = golemConditioning,
                    workspaceId = workspaceId,
                    phenomenalFlow = workspace.expressions().toList(),
                    hints = emptyMap()
                ).collect { event ->
                    cognitionBroadcaster.emit(event)
                }

                val culminatedWithIntent = repository.maybeCulminatedWithIntent(
                    workspaceId
                )?.let { intent ->

                    // TODO it should be solved better in the future
                    val agent = EpistemicAgent.Computer(
                        id = identity.getComputerId(),
                    )

                    val expressionInfo = repository.initiateExpression(
                        workspaceId = workspaceId,
                        agent = agent
                    )

                    cognitionBroadcaster.emit(CognitionEvent.ExpressionInitiation(
                        expressionId = expressionInfo.id,
                        agent = agent,
                        moment = expressionInfo.initiationMoment
                    ))

                    val fulfilmentId = repository.initiateFulfilmentPhenomenon(
                        workspaceId = workspaceId,
                        expressionId = expressionInfo.id,
                        systemId = intent.systemId
                    )

                    cognitionBroadcaster.emit(CognitionEvent.FulfillmentInitiation(
                        id = fulfilmentId,
                        expressionId = expressionInfo.id
                    ))

//                    // TODO why we need this async?
//                    val deferred = scope.async {
//                        actualize(
//                            workspaceId = workspaceId,
//                            expressionId = expressionInfo.id,
//                            phenomenonId = fulfilmentId,
//                            intent = intent
//                        )
//                    }.await()!!

                    val result = scriptExecutor.execute(
                        script = intent.code,
                        dependencies = golemScriptDependencies
                    )

                    val (text, impeded) = when (result) {
                        is ExecuteGolemScript.Result.Value -> result.value.toString() to false
                        is ExecuteGolemScript.Result.Error -> result.message to true
                    }

                    repository.appendText(
                        workspaceId = workspaceId,
                        expressionId = expressionInfo.id,
                        phenomenonId = fulfilmentId,
                        textDelta = text
                    )

                    cognitionBroadcaster.emit(CognitionEvent.FulfillmentUnfolding(
                        id = fulfilmentId,
                        expressionId = expressionInfo.id,
                        textDelta = text
                    ))

                    cognitionBroadcaster.emit(CognitionEvent.FulfillmentCulmination(
                        id = fulfilmentId,
                        expressionId = expressionInfo.id,
                        impeded = impeded
                    ))

                    val moment = repository.culminateExpression(
                        workspaceId = workspaceId,
                        expressionId = expressionInfo.id
                    )

                    cognitionBroadcaster.emit(CognitionEvent.ExpressionCulmination(
                        expressionId = expressionInfo.id,
                        moment = moment
                    ))

                    true
                } ?: false

            } while (culminatedWithIntent)
        }
    }

//    private suspend fun actualize(
//        workspaceId: Long,
//        expressionId: Long,
//        phenomenonId: Long,
//        intent: Phenomenon.Intent,
//    ): List<Phenomenon>? {
//
//        logger.debug {
//            "Workspace[$workspaceId]/Expression[${expressionId}]/Phenomenon[$phenomenonId]: " +
//                    "Actualizing intent, purpose: ${intent.purpose}, code: ${intent.code}"
//        }
//
//
//
//        val phenomena = when (result) {
//            is ExecuteGolemScript.Result.Value -> when(result.value) {
//                is String -> listOf(
//                    Phenomenon.Fulfillment(
//                        intentId = intent.id,
//                        intentSystemId = intent.systemId,
//                        result = result.value
//                    )
//                )
//                is Unit -> null
//                else -> listOf(
//                    Phenomenon.Text(
//                        id = Uuid.random().toString(),
//                        text = result.value.toString()
//                    )
//                )
//            }
//            is ExecuteGolemScript.Result.Error -> listOf(Phenomenon.Impediment(
//                id = Uuid.random().toString(),
//                intentId = intent.id,
//                intentSystemId = intent.systemId,
//                reason = result.message
//            ))
//        }
//
//        return phenomena
//    }

    fun interruptCognition(workspaceId: Long) {
        // TODO better implementation, should join, send Interrupted event and remove from activeCognitionMap
        activeCognitionMap[workspaceId]!!.cancel()
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

    // used to send back initial expression via websocket

}
