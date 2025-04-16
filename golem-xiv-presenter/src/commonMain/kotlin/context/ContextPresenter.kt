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
import com.xemantic.ai.golem.api.GolemInput
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
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

interface ContextView {

    fun addMessage(message: Message)

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
    golemInputCollector: FlowCollector<GolemInput>
) {

    private val logger = KotlinLogging.logger {}

    // TODO it should be a proper child scope
    private val scope: CoroutineScope = MainScope()

    private var isShift: Boolean = false

    private var currentPrompt: String = ""

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
                if (prompt.isNotEmpty() && (prompt.last() == '\n' != isShift)) {
                    golemInputCollector.emit(
                        GolemInput.Prompt(message = Message(content = listOf(Text(prompt))))
                    )
                }
                view.updatePromptInputHeight()
            }
        }
//        mainScope.launch {
//            view.promptSubmits.collect {
//                if (currentPrompt.isNotBlank()) {
//                    view.submitsDisabled(true)
//                    view.addTextResponse(currentPrompt)
//                    view.clearPromptInput()
//                    golemInputCollector.emit(
//                        GolemInput.Prompt(message = Message(content = listOf(Text(currentPrompt))))
//                    )
//                }
//            }
//        }

//        scope.launch {
//            reasoningEvents.collect { output ->
//                when (output) {
//                    is GolemOutput.Welcome -> {
//                        view.addWelcomeMessage(output.message)
//                        view.submitsDisabled(false)
//                    }
//                    else -> throw IllegalStateException("unknown event")
////                    is ReasoningEvent.ModelResponse -> {
////                        view.submitsDisabled(
////                            output.messageResponse.stopReason == StopReason.TOOL_USE
////                        )
////                        output.messageResponse.content.forEach {
////                            if (it is Text) {
////                                view.addTextResponse(it.text)
////                            }
////                        }
////                    }
////                    is AgentOutput.ToolUseRequest -> {
////                        view.addToolUseRequest(output)
////                    }
////                    is AgentOutput.ToolUseResponse -> {
////                        view.addToolUseResponse(output)
////                    }
////                    is AgentOutput.Error -> {
////                        view.submitsDisabled(false)
////                    }
//                }
//            }
//        }
    }

    suspend fun loadContext(id: Uuid): Boolean {
        logger.info { "Loading context: $id" }
        val context = scope.async(ioDispatcher) {
            contextService.get(id)
        }.await()
        if (context == null) return false
        context.messages.forEach {
            view.addMessage(it)
        }
        return true
    }

    fun dispose() {
        scope.cancel()
    }

}
