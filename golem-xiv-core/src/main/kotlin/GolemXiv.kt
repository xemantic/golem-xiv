/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
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

package com.xemantic.ai.golem.core

import com.xemantic.ai.golem.api.CognitionListItem
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.backend.CognitionRepository
import com.xemantic.ai.golem.api.backend.Cognizer
import com.xemantic.ai.golem.api.backend.AgentIdentity
import com.xemantic.ai.golem.api.backend.TitleGenerator
import com.xemantic.ai.golem.api.backend.script.ExecuteGolemScript
import com.xemantic.ai.golem.core.kotlin.getClasspathResource
import com.xemantic.ai.golem.core.script.GolemScriptExecutor
import com.xemantic.ai.golem.core.kotlin.describeCurrentMoment
import com.xemantic.ai.golem.core.os.operatingSystemName
import com.xemantic.ai.golem.core.script.GolemScriptDependencyProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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

val golemMainConstitution = getClasspathResource("/constitution/GolemXivConstitution.md")

class GolemXiv(
    private val identity: AgentIdentity,
    private val repository: CognitionRepository,
    private val cognizer: Cognizer,
    private val titleGenerator: TitleGenerator?,
    private val golemScriptDependencyProvider: GolemScriptDependencyProvider,
    private val outputs: FlowCollector<GolemOutput>
) : AutoCloseable {

    private val logger = KotlinLogging.logger {}

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // In the future it should be some form of distributed marker
    private val activeCognitionMap = ConcurrentHashMap<Long, Job>()

    private val scriptExecutor = GolemScriptExecutor()

    val golemConstitution = buildList {
        //val coreSystem = systemPrompt + if (golemScriptApi != null) GOLEM_SCRIPT_SYSTEM_PROMPT else ""
        add(golemMainConstitution)
        add(environmentalContext())
//            if (additionalSystemPrompt != null) {
//                add(System(text = additionalSystemPrompt)) // TODO cache control
//            }
    }

    suspend fun initiateCognition(): Long {
        val info = repository.initiateCognition(
            constitution = golemConstitution
        )
        return info.id
    }

    suspend fun listCognitions(
        limit: Int = 50,
        offset: Int = 0
    ): List<CognitionListItem> = repository.listCognitions(limit, offset)

    /**
     * @throws com.xemantic.ai.golem.api.backend.GolemException
     */
    suspend fun perceive(
        cognitionId: Long,
        phenomena: List<Phenomenon>
    ) {

        // TODO check if cognition is active
        logger.debug { "Cognition[$cognitionId]: perceiving phenomena" }

        val phenomenalExpression = repository.appendPhenomena(
            cognitionId = cognitionId,
            agent = EpistemicAgent.Human(
                id = identity.getUserId("foo") // TODO where to keep this mapping?
            ),
            phenomena = phenomena
        )

        val cognitionBroadcaster = outputs.cognitionBroadcaster(cognitionId)

        cognitionBroadcaster.emit(phenomenalExpression)

        // Generate title for first user message
        if (titleGenerator != null && repository.isFirstExpression(cognitionId)) {
            val firstText = phenomena.filterIsInstance<Phenomenon.Text>().firstOrNull()
            if (firstText != null) {
                scope.launch {
                    try {
                        val title = titleGenerator.generateTitle(firstText.text)
                        repository.getCognition(cognitionId).setTitle(title)
                        outputs.emit(GolemOutput.CognitionTitleUpdated(cognitionId, title))
                    } catch (e: Exception) {
                        logger.warn(e) { "Failed to generate title for cognition $cognitionId" }
                    }
                }
            }
        }

        activeCognitionMap[cognitionId] = scope.launch {

            logger.debug { "Cognition[$cognitionId]: Initiating cognition" }

            do {

                val cognition = repository.getCognition(cognitionId)

                cognizer.reason(
                    constitution = golemConstitution,
                    cognitionId = cognitionId,
                    phenomenalFlow = cognition.expressions().toList(),
                    hints = emptyMap()
                ).collect { event ->
                    cognitionBroadcaster.emit(event)
                }

                val culminatedWithIntent = repository.maybeCulminatedWithIntent(
                    cognitionId
                )?.let { intent ->

                    // TODO it should be solved better in the future
                    val agent = EpistemicAgent.Computer(
                        id = identity.getComputerId(),
                    )

                    val expressionInfo = repository.initiateExpression(
                        cognitionId = cognitionId,
                        agent = agent
                    )

                    cognitionBroadcaster.emit(CognitionEvent.ExpressionInitiation(
                        expressionId = expressionInfo.id,
                        agent = agent,
                        moment = expressionInfo.initiationMoment
                    ))

                    val fulfillmentId = repository.initiateFulfillmentPhenomenon(
                        cognitionId = cognitionId,
                        expressionId = expressionInfo.id,
                        intentId = intent.id,
                        systemId = intent.systemId
                    )

                    cognitionBroadcaster.emit(CognitionEvent.FulfillmentInitiation(
                        id = fulfillmentId,
                        expressionId = expressionInfo.id,
                        intentId = intent.id,
                        intentSystemId = intent.systemId
                    ))

//                    // TODO why we need this async?
//                    val deferred = scope.async {
//                        actualize(
//                            cognitionId = cognitionId,
//                            expressionId = expressionInfo.id,
//                            phenomenonId = fulfillmentId,
//                            intent = intent
//                        )
//                    }.await()!!

                    val result = scriptExecutor.execute(
                        script = intent.code,
                        dependencies = golemScriptDependencyProvider.dependencies(
                            cognitionId = cognitionId,
                            fulfillmentId = fulfillmentId
                        )
                    )

                    val (text, impeded) = when (result) {
                        is ExecuteGolemScript.Result.Value -> result.value.toString() to false
                        is ExecuteGolemScript.Result.Error -> result.message to true
                    }

                    repository.appendText(
                        cognitionId = cognitionId,
                        expressionId = expressionInfo.id,
                        phenomenonId = fulfillmentId,
                        textDelta = text
                    )

                    cognitionBroadcaster.emit(CognitionEvent.FulfillmentUnfolding(
                        id = fulfillmentId,
                        expressionId = expressionInfo.id,
                        textDelta = text
                    ))

                    cognitionBroadcaster.emit(CognitionEvent.FulfillmentCulmination(
                        id = fulfillmentId,
                        expressionId = expressionInfo.id,
                        impeded = impeded
                    ))

                    val moment = repository.culminateExpression(
                        cognitionId = cognitionId,
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
//        cognitionId: Long,
//        expressionId: Long,
//        phenomenonId: Long,
//        intent: Phenomenon.Intent,
//    ): List<Phenomenon>? {
//
//        logger.debug {
//            "Cognition[cognitionId]/Expression[${expressionId}]/Phenomenon[$phenomenonId]: " +
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

    fun interruptCognition(cognitionId: Long) {
        // TODO better implementation, should join, send Interrupted event and remove from activeCognitionMap
        activeCognitionMap[cognitionId]!!.cancel()
    }

    fun emitCognition(id: Long) {
        scope.launch {
            val cognitionBroadcaster = outputs.cognitionBroadcaster(id)
            repository.getCognition(id).expressions().collect { expression ->
                cognitionBroadcaster.emit(expression)
            }
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

    // used to send back initial expression via websocket

}
