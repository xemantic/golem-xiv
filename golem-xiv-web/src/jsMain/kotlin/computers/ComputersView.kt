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

package com.xemantic.golem.web.computers

import com.xemantic.golem.web.ElementBuilder
import com.xemantic.kotlin.js.dom.html.*

fun ElementBuilder.computersView() {

    article("large-padding") {
        h6 {
            icon("warning", klass = "extra")
            +" The Computers section is under construction"
        }
        p {
            +"It will allows to:"
        }
        ul {
            li { +"Review active VMs allocated by Golem" }
            li { +"Close them if no longer needed" }
        }
    }

}
