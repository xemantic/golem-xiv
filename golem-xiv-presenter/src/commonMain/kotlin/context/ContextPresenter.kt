/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.context

import com.xemantic.ai.golem.api.Message
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.Prompt
import com.xemantic.ai.golem.api.ReasoningEvent
import com.xemantic.ai.golem.api.Text
import com.xemantic.ai.golem.api.service.ContextService
import com.xemantic.ai.golem.presenter.util.Action
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

interface MessageAppender {

    fun append(text: String)

    fun finalize()

}

interface ContextView {

    fun addMessage(message: Message)

    fun startMessage(role: Message.Role): MessageAppender

    val promptChanges: Flow<String>

    val promptInputShiftKeys: Flow<Boolean>

    fun updatePromptInputHeight()

    val sendActions: Flow<Action>

    var promptInputDisabled: Boolean

    var sendDisabled: Boolean

    fun clearPromptInput()

    fun addTextResponse(text: String)

//    fun addToolUseRequest(request: AgentOutput.ToolUseRequest)
//
//    fun addToolUseResponse(response: AgentOutput.ToolUseResponse)

}

class ContextPresenter(
    mainScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
    private val contextService: ContextService,
    private val view: ContextView,
    private val golemOutputs: Flow<GolemOutput>
) {

    private val logger = KotlinLogging.logger {}

    // TODO it should be a proper child scope
    private val scope: CoroutineScope = MainScope()

    private var isShift: Boolean = false

    private var currentPrompt: String = ""

    private var contexId: Uuid? = null

    private val messageAppenderMap = mutableMapOf<Uuid, MessageAppender>()

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
            golemOutputs.filterIsInstance<GolemOutput.Reasoning>().filter {
                it.contextId == contexId
            }.map {
                it.event
            }.collect {
                logger.info { "$it" }
                when (it) {
                    is ReasoningEvent.MessageStart -> {
                        messageAppenderMap[it.messageId] = view.startMessage(it.role)
                    }
                    is ReasoningEvent.TextContentDelta -> {
                        messageAppenderMap[it.messageId]!!.append(it.delta)
                    }
                    else -> {}
                }
            }
        }

    }

    suspend fun loadContext(id: Uuid): Boolean {
        logger.info { "Loading context: $id" }
        val context = scope.async(ioDispatcher) {
            contextService.get(id)
        }.await()
        if (context == null) return false
//        context.messages.forEach {
//            view.addMessage(it)
//        }
        return true
    }

    fun dispose() {
        scope.cancel()
    }

    private suspend fun sendPrompt() {
        view.sendDisabled = true
        view.clearPromptInput()
        if (contexId == null) {
            val context = startContext()
            contexId = context.id
        } else {
            appendToContext()
        }
    }

    private suspend fun startContext() = withContext(ioDispatcher) {
        contextService.start(Prompt(listOf(Text(currentPrompt))))
    }

    private suspend fun appendToContext() = withContext(ioDispatcher) {
        contextService.append(contexId!!, Prompt(listOf(Text(currentPrompt))))
    }

}
