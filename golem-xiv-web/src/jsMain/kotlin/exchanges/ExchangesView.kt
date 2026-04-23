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

package com.xemantic.golem.web.exchanges

import com.xemantic.golem.web.ElementBuilder
import com.xemantic.kotlin.js.dom.html.*

fun ElementBuilder.exchangesView() {

    article("large-padding") {
        h6 {
            icon("warning", klass = "extra")
            +" The Exchanges section is under construction"
        }
        p {
            +"""
                Since Golem is deployed in private organizations, everything which qualifies
                as a communication with external parties is a subject to structured monitoring.
            """.trimIndent()
            +"""
                Think about it as a mailbox, where emails are received and sent by Golem.
            """.trimIndent()
        }
        p {
            +"The Exchanges section will allow to:"
        }
        ul {
            li { +"Review communication traces between Golem and the outside world" }
            li { +"Email is intended as a default communication medium" }
            li { +"Other communicators - Slack, Signal, Telegram, etc. can be also added" }
        }
    }

}
