/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.golem.web.settings

import com.xemantic.golem.viewmodel.settings.SettingsStrings
import com.xemantic.golem.web.ElementBuilder
import com.xemantic.kotlin.js.dom.NodeBuilder
import com.xemantic.kotlin.js.dom.html.*
import org.w3c.dom.HTMLUListElement

fun ElementBuilder.settingView(
    strings: SettingsStrings
) {

    ul("list border") {
        item(strings.Profile, icon = "person")
        item(strings.Secrets, icon = "lock")
        item(strings.Users, icon = "people")
        item(strings.Appearance, icon = "palette")
        item(strings.Devices, icon = "devices_other")
    }

    article("large-padding") {
        h6 {
            icon("warning", klass = "extra")
            +" The Settings section is under construction"
        }
        p {
            +"Currently only server side configuration is supported. The Settings will allow to:"
        }
        ul {
            li { +"Manage API keys of intelligence providers" }
            li { +"Manage users and their permissions" }
            li { +"Set system-wide passwords and access credentials" }
            li { +"Personalize experience" }
            li { +"Manage user's devices used by Golem as the sensorioum" }
        }
    }

}

private fun NodeBuilder<HTMLUListElement>.item(
    title: String,
    icon: String
) {
    li {
        a("wave") {
            icon(name = icon)
            div("max") {
                h6("small") { +title }
            }
        }
    }
}
