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

import com.xemantic.ai.golem.presenter.navigation.HeaderView
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.web.js.actions
import com.xemantic.ai.golem.web.js.ariaLabel
import com.xemantic.ai.golem.web.js.icon
import com.xemantic.ai.golem.web.util.inject
import com.xemantic.ai.golem.web.view.HtmlView
import kotlinx.coroutines.flow.Flow
import kotlinx.html.*
import kotlinx.html.js.header

class HtmlHeaderView() : HeaderView, HtmlView {

    private val themeIcon = html.button(classes = "menu-toggle") {
        ariaLabel = "Toggle sidebar menu"
        icon("bars")
    }

    override val element = html.header {
        nav {
            div("nav-left")
            div("nav-center") {
                div("logo") {
                    +"Golem XIV"
                }
            }
            div("nav-right") {
                // Empty div to keep the centered title balanced
            }
        }
    }.inject(
        themeIcon to ".nav-left"
    )

    override val toggleMenuClicks: Flow<Action> = themeIcon.actions()

}
