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

package com.xemantic.golem.web.solicitations

import com.xemantic.golem.web.ElementBuilder
import com.xemantic.kotlin.js.dom.html.*

fun ElementBuilder.solicitationsView() {

    article("large-padding") {
        h6 {
            icon("warning", klass = "extra")
            +" The Solicitations section is under construction"
        }
        p {
            +"""
                A solicitation represents any environmental trigger which is initiating a cogitation
                - an instance of Golem's cognitive process
            """.trimIndent()
        }
        p {
            +"Example solicitations:"
        }
        ul {
            li { +"We reached certain moment (one time or periodically - cron)" }
            li { +"An email addressed to Golem has arrived" }
            li { +"Certain facts appeared in the DB (direct change notification)  / Knowledge Graph" }
            li { +"Rule-based triggering" }
        }
        p {
            +"The Exchanges section will allow to manage:"
        }
        ul {
            li { +"Time based triggers (e.g. cron) scheduled by Golem before" }
            li { +"Other solicitation - Email Slack, Signal, Telegram, etc." }
        }
    }

}
