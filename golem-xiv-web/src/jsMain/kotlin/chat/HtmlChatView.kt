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

package com.xemantic.ai.golem.web.chat

import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.view.HtmlView
import kotlinx.browser.document
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.div

class HtmlChatView : HtmlView {

    override val element = document.create.div("chat-centered-mode") {
        div {
            id = "messages"
            div {
                id = "prompt-box"
                textArea {
                    id = "prompt-input"
                    placeholder = "Ask me anything..."
                }
                div("input-buttons") {
                    button {
                        id = "mic-button"
                        ariaLabel = "Start voice input"
                        i("fas fa-microphone")
                    }
                    button {
                        id = "send-button"
                        ariaLabel = "Send message"
                        i("fas fa-paper-plane")
                    }
                }
                div("hidden") {
                    id = "mic-status"
                    +"Listening... "
                    span {
                        id = "recording-time"
                        +"0.0"
                    }
                    button {
                        id = "stop-recording"
                        i("fas fa-stop")
                    }
                }
            }
        }
    }

}