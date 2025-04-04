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
import kotlinx.html.js.header

class HtmlHeaderView : HtmlView {

    override val element = document.create.header {
        nav {
            div("nav-left") {
                button(classes = "menu-toggle") {
                    ariaLabel = "Toggle sidebar menu"
                    i("fas fa-bars")
                }
            }
            div("nav-center") {
                div("logo") {
                    +"Golem XIV"
                }
            }
            div("nav-right") {
                // Empty div to keep the centered title balanced
            }
        }
    }

}
