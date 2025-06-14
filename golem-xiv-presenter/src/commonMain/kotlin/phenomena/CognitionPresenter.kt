/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.phenomena // TODO maybe it should be rather cognition?

import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.EpistemicAgent
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.client.CognitionService
import com.xemantic.ai.golem.presenter.ScreenView
import com.xemantic.ai.golem.presenter.util.Action
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

typealias TextAppender = (text: String) -> Unit

interface ExpressionAppender {

    fun textAppender(): TextAppender

    fun intentAppender(): IntentAppender

}

interface IntentAppender {

    fun purposeAppender(): TextAppender

    fun codeAppender(): TextAppender

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

    fun clearPromptInput()

//    fun addTextResponse(text: String)

//    fun addToolUseRequest(request: AgentOutput.ToolUseRequest)
//
//    fun addToolUseResponse(response: AgentOutput.ToolUseResponse)

}

class CognitionPresenter(
    mainScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
    private val cognitionService: CognitionService,
    private val view: CognitionView,
    private val golemOutputs: Flow<GolemOutput>
) {

    private val logger = KotlinLogging.logger {}

    private val scope: CoroutineScope = CoroutineScope(
        mainScope.coroutineContext + SupervisorJob()
    )

    private var isShift: Boolean = false

    private var currentPrompt: String = ""

    private var cognitionId: Long? = null

    private val expressionAppenderMap = mutableMapOf<Long, ExpressionAppender>()

    init {

        view.sendDisabled = true

        view.promptInputShiftKeys.onEach {
            isShift = it
        }.launchIn(scope)

        view.promptChanges.onEach { prompt ->
            currentPrompt = prompt
            view.sendDisabled = prompt.isBlank()
            if (prompt.isNotBlank() && (prompt.last() == '\n' && !isShift)) {
                sendPrompt()
            }
            view.updatePromptInputHeight()
        }.launchIn(scope)

        view.sendActions.onEach {
            if (currentPrompt.isNotBlank()) {
                sendPrompt()
            }
        }.launchIn(scope)

        scope.launch {
            var intentAppender: IntentAppender? = null
            var textAppender: TextAppender? = null
            var purposeAppender: TextAppender? = null
            var codeAppender: TextAppender? = null
            golemOutputs.filterIsInstance<GolemOutput.Cognition>().filter {
                it.cognitionId == cognitionId
            }.map {
                it.event
            }.collect {
                logger.info { "$it" }
                when (it) {
                    is CognitionEvent.ExpressionInitiation -> {
                        expressionAppenderMap[it.expressionId] = view.starExpression(it.agent)
                    }
                    is CognitionEvent.TextInitiation -> {
                        textAppender = expressionAppenderMap[it.expressionId]!!.textAppender()
                    }
                    is CognitionEvent.TextUnfolding -> {
                        textAppender!!(it.textDelta)
                    }
                    is CognitionEvent.IntentInitiation -> {
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
                    else -> {}
                }
            }
        }

    }

    fun dispose() {
        scope.cancel()
    }

    private suspend fun sendPrompt() {
        view.sendDisabled = true
        view.clearPromptInput()
        if (cognitionId == null) {
            cognitionId = initiateCognition()
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
