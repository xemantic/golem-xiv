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

package com.xemantic.golem.web.reasoning

import com.xemantic.ai.golem.api.Message
import com.xemantic.ai.golem.api.ReasoningEvent
import com.xemantic.ai.golem.api.UserEvent
import com.xemantic.golem.web.ui.ReasoningView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ReasoningPresenter(
    scope: CoroutineScope,
    view: ReasoningView,
    reasoning: List<Message>, // existing reasoning
    reasoningEvents: Flow<ReasoningEvent>,
    send: suspend (UserEvent) -> Unit
) {

    private var currentPrompt = ""

    init {
        view.promptSubmitDisabled = true
        scope.launch {
            view.promptChanges.collect { prompt ->
                currentPrompt = prompt
                view.promptSubmitDisabled = currentPrompt.isBlank()
            }
        }
        scope.launch {
            view.promptSubmits.collect {
                if (currentPrompt.isNotBlank()) {
                    view.submitsDisabled(true)
                    view.addTextResponse(currentPrompt)
                    view.clearPromptInput()
                    //send(UserEvent.Prompt(Message(content = Text(currentPrompt))))
                }
            }
        }
        scope.launch {
            reasoningEvents.collect { output ->
                when (output) {
                    is ReasoningEvent.Welcome -> {
                        view.addWelcomeMessage(output.message)
                        view.submitsDisabled(false)
                    }
                    else -> throw IllegalStateException("unknown event")
//                    is ReasoningEvent.ModelResponse -> {
//                        view.submitsDisabled(
//                            output.messageResponse.stopReason == StopReason.TOOL_USE
//                        )
//                        output.messageResponse.content.forEach {
//                            if (it is Text) {
//                                view.addTextResponse(it.text)
//                            }
//                        }
//                    }
//                    is AgentOutput.ToolUseRequest -> {
//                        view.addToolUseRequest(output)
//                    }
//                    is AgentOutput.ToolUseResponse -> {
//                        view.addToolUseResponse(output)
//                    }
//                    is AgentOutput.Error -> {
//                        view.submitsDisabled(false)
//                    }
                }
            }
        }
    }

}

private fun ReasoningView.submitsDisabled(disabled: Boolean) {
    promptInputDisabled = disabled
    promptSubmitDisabled = disabled
}
