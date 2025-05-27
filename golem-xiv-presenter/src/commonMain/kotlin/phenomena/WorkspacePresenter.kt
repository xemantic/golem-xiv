/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.phenomena // TODO maybe it should be rather workspace?

import com.xemantic.ai.golem.api.Agent
import com.xemantic.ai.golem.api.Expression
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.service.CognitiveWorkspaceService
import com.xemantic.ai.golem.presenter.ScreenView
import com.xemantic.ai.golem.presenter.util.Action
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
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

interface CognitiveWorkspaceView : ScreenView {

    fun addExpression(expression: Expression)

    fun starExpression(agent: Agent): ExpressionAppender

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

class WorkspacePresenter(
    mainScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
    private val cognitiveWorkspaceService: CognitiveWorkspaceService,
    private val view: CognitiveWorkspaceView,
    private val golemOutputs: Flow<GolemOutput>
) {

    private val logger = KotlinLogging.logger {}

    // TODO it should be a proper child scope
    private val scope: CoroutineScope = MainScope()

    private var isShift: Boolean = false

    private var currentPrompt: String = ""

    private var workspaceId: String? = null

    private val expressionAppenderMap = mutableMapOf<String, ExpressionAppender>()

    init {

        view.sendDisabled = true

        scope.launch {
            view.promptInputShiftKeys.collect {
                isShift = it
            }
        }

        scope.launch {
            view.promptChanges.collect { prompt ->
                currentPrompt = prompt
                view.sendDisabled = prompt.isBlank()
                if (prompt.isNotBlank() && (prompt.last() == '\n' && !isShift)) {
                    sendPrompt()
                }
                view.updatePromptInputHeight()
            }
        }

        scope.launch {
            view.sendActions.collect {
                if (currentPrompt.isNotBlank()) {
                    sendPrompt()
                }
            }
        }

        scope.launch {
            var intentAppender: IntentAppender? = null
            var textAppender: TextAppender? = null
            var purposeAppender: TextAppender? = null
            var codeAppender: TextAppender? = null
            golemOutputs.filterIsInstance<GolemOutput.Cognition>().filter {
                it.workspaceId == workspaceId
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

//    suspend fun loadContext(id: Uuid): Boolean {
//        logger.info { "Loading context: $id" }
//        val context = scope.async(ioDispatcher) {
//            cognitiveWorkspaceService.get(id)
//        }.await()
//        if (context == null) return false
////        context.messages.forEach {
////            view.addMessage(it)
////        }
//        return true
//    }

    fun dispose() {
        scope.cancel()
    }

    private suspend fun sendPrompt() {
        view.sendDisabled = true
        view.clearPromptInput()
        if (workspaceId == null) {
            workspaceId = initiateWorkspace()
        } else {
            integrateWithWorkspace()
        }
    }

    private suspend fun initiateWorkspace(): String = withContext(ioDispatcher) {
        cognitiveWorkspaceService.initiate(
            phenomena = listOf(Phenomenon.Text(id = "N/A", currentPrompt))
        )
    }

    private suspend fun integrateWithWorkspace() = withContext(ioDispatcher) {
        cognitiveWorkspaceService.integrate(
            workspaceId = workspaceId!!,
            phenomena = listOf(Phenomenon.Text(id = "N/A", currentPrompt))
        )
    }

}
