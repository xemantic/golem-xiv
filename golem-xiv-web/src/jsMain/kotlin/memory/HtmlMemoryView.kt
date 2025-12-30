/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
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

package com.xemantic.ai.golem.web.memory

import com.xemantic.ai.golem.presenter.memory.MemoryView
import com.xemantic.ai.golem.web.view.HasRootHtmlElement
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.iframe
import org.w3c.dom.HTMLElement

class HtmlMemoryView() : MemoryView, HasRootHtmlElement {

    // TODO it should be populated with local storage if DB is remote
    override val element: HTMLElement = document.create.iframe(classes = "memory") {
        src = "neo4j-browser/?dbms=bolt://localhost:7687&preselectAuthMethod=NO_AUTH"
    }

}
