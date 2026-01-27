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

package com.xemantic.ai.golem.presenter.cognition

import com.xemantic.ai.golem.api.*
import com.xemantic.ai.golem.api.client.CognitionService
import com.xemantic.ai.golem.presenter.ScreenView
import com.xemantic.ai.golem.presenter.navigation.Navigation
import com.xemantic.ai.golem.presenter.util.Action
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

typealias TextAppender = (text: String) -> Unit

interface ExpressionAppender {

    fun textAppender(): TextAppender

    fun intentAppender(): IntentAppender

    fun fulfillmentAppender(): FulfillmentAppender

}

interface IntentAppender {

    fun purposeAppender(): TextAppender

    fun codeAppender(): TextAppender

}

interface FulfillmentAppender {

    fun textAppender(): TextAppender

}

interface CognitionView : ScreenView {

    fun addExpression(expression: PhenomenalExpression)

    fun starExpression(agent: EpistemicAgent): ExpressionAppender

    val promptChanges: Flow<String>

    val promptInputShiftKeys: Flow<Boolean>

    fun updatePromptInputHeight()

    val sendActions: Flow<Action>

    var promptInputDisabled: Boolean

    var sendDisabled: Boolean

    var cognizing: Boolean

    fun clearPromptInput()

//    fun addTextResponse(text: String)

//    fun addToolUseRequest(request: AgentOutput.ToolUseRequest)
//
//    fun addToolUseResponse(response: AgentOutput.ToolUseResponse)

}

class CognitionPresenter(
    private var cognitionId: Long? = null,
    private val view: CognitionView,
    mainScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
    private val cognitionService: CognitionService,
    golemOutputs: Flow<GolemOutput>,
    private val navigation: Navigation
) {

    private val logger = KotlinLogging.logger {}

    private val scope: CoroutineScope = CoroutineScope(
        mainScope.coroutineContext + SupervisorJob()
    )

    private var isShift: Boolean = false

    private var currentPrompt: String = ""

    private val expressionAppenderMap = mutableMapOf<Long, ExpressionAppender>()
    private val aiExpressionIds = mutableSetOf<Long>()
    private val expressionsWithIntent = mutableSetOf<Long>()

    init {

        view.sendDisabled = true

        view.promptInputShiftKeys.onEach {
            isShift = it
        }.launchIn(scope)

        view.promptChanges.onEach { prompt ->
            currentPrompt = prompt
            view.sendDisabled = prompt.isBlank()
            if (prompt.isNotBlank() && (prompt.last() == '\n' && !isShift)) {
                sendPhenomena()
            }
            view.updatePromptInputHeight()
        }.launchIn(scope)

        view.sendActions.onEach {
            if (currentPrompt.isNotBlank()) {
                sendPhenomena()
            }
        }.launchIn(scope)

        var intentAppender: IntentAppender? = null
        var fulfillmentAppender: FulfillmentAppender? = null
        var textAppender: TextAppender? = null
        var purposeAppender: TextAppender? = null
        var codeAppender: TextAppender? = null
        var fulfillmentTextAppender: TextAppender? = null

        golemOutputs.filterIsInstance<GolemOutput.Cognition>().filter {
            it.cognitionId == cognitionId
            true
        }.map {
            it.event
        }.onEach {
            logger.info { "$it" }
            when (it) {
                is CognitionEvent.ExpressionInitiation -> {
                    expressionAppenderMap[it.expressionId] = view.starExpression(it.agent)
                    if (it.agent is EpistemicAgent.AI) {
                        aiExpressionIds.add(it.expressionId)
                    }
                }
                is CognitionEvent.ExpressionCulmination -> {
                    val wasAiExpression = aiExpressionIds.remove(it.expressionId)
                    val hadIntent = expressionsWithIntent.remove(it.expressionId)
                    if (wasAiExpression && !hadIntent) {
                        view.cognizing = false
                    }
                }
                is CognitionEvent.TextInitiation -> {
                    textAppender = expressionAppenderMap[it.expressionId]!!.textAppender()
                }
                is CognitionEvent.TextUnfolding -> {
                    textAppender!!(it.textDelta)
                }
                is CognitionEvent.IntentInitiation -> {
                    expressionsWithIntent.add(it.expressionId)
                    intentAppender = expressionAppenderMap[it.expressionId]!!.intentAppender()
                }
                is CognitionEvent.IntentPurposeInitiation -> {
                    purposeAppender = intentAppender!!.purposeAppender()
                }
                is CognitionEvent.IntentPurposeUnfolding -> {
                    purposeAppender!!(it.purposeDelta)
                }
                is CognitionEvent.IntentCodeInitiation -> {
                    codeAppender = intentAppender!!.codeAppender()
                }
                is CognitionEvent.IntentCodeUnfolding -> {
                    codeAppender!!(it.codeDelta)
                }
                is CognitionEvent.FulfillmentInitiation -> {
                    fulfillmentAppender = expressionAppenderMap[it.expressionId]!!.fulfillmentAppender()
                    fulfillmentTextAppender = fulfillmentAppender.textAppender()
                }
                is CognitionEvent.FulfillmentUnfolding -> {
                    fulfillmentTextAppender!!(it.textDelta)
                }
                else -> {}
            }
        }.launchIn(scope)

    }

    fun dispose() {
        scope.cancel()
    }

    private suspend fun sendPhenomena() {
        view.sendDisabled = true
        view.cognizing = true
        view.clearPromptInput()
        if (cognitionId == null) {
            cognitionId = initiateCognition()
            navigation.navigateTo(Navigation.Target.Cognition(id = cognitionId!!))
        } else {
            continueCognition()
        }
    }

    private suspend fun initiateCognition(): Long = withContext(ioDispatcher) {
        cognitionService.initiateCognition(
            phenomena = listOf(Phenomenon.Text(id = -1, currentPrompt))
        )
    }

    private suspend fun continueCognition() = withContext(ioDispatcher) {
        cognitionService.continueCognition(
            id = cognitionId!!,
            phenomena = listOf(Phenomenon.Text(id = -1, currentPrompt))
        )
    }

}
