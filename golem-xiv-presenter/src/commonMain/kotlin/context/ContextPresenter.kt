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

    private var currentMessageAppender: MessageAppender? = null

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
                if (prompt.isNotBlank() && (prompt.last() == '\n' != isShift)) {
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
            golemOutputs.onEach {
                logger.info { "Received in presenter: $it" }
            }.filterIsInstance<GolemOutput.Reasoning>()
            .filter {
                logger.info { "Filtering - current contextId: $contexId" }
                logger.info { "Condition: ${it.contextId == contexId}" }
                it.contextId == contexId
            }.map {
                it.event
            }.collect {
                when (it) {
                    is ReasoningEvent.MessageStart -> {
                        currentMessageAppender = view.startMessage(it.role)
                    }
                    is ReasoningEvent.TextContentDelta -> {
                        currentMessageAppender!!.append(it.delta)
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
