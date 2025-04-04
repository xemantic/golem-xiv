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

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.view.HtmlView
import kotlinx.browser.document
import kotlinx.html.*
import kotlinx.html.dom.create

class HtmlSidebarView() : HtmlView {

    private val conversationList = document.create.ul("conversation-list") {
        li("no-conversations") {
            +"No conversations yet"
        }
    }

    private val toggleThemeButton = document.create.button {
        ariaLabel = "Toggle dark/light theme"
        i("fas fa-moon")
        span {
            +"Toggle Theme"
        }
    }

    override val element = document.create.aside("sidebar") {
        div("sidebar-header") {
            h2("Conversation")
            button(classes = "new-chat-btn") {
                i("fas fa-plus"); +" New Chat"
            }
        }
        div("sidebar-content")
        div("sidebar-footer")
    }.apply {
        querySelector(".sidebar-content")!!.append(conversationList)
        querySelector(".sidebar-footer")!!.append(toggleThemeButton)
    }

}
