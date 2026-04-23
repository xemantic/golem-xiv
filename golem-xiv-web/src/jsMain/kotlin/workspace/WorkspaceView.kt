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

package com.xemantic.golem.web.workspace

import com.xemantic.golem.web.ElementBuilder
import com.xemantic.kotlin.js.dom.html.*

fun ElementBuilder.workspaceView() {

    article("large-padding") {
        h6 {
            icon("warning", klass = "extra")
            +" The Workspace section is under construction"
        }
        p {
            +"""
                The Workspace is like Obsidian / Google Drive / GitHub combination.
            """.trimIndent()
        }
        p {
            +"Features:"
        }
        ul {
            li { +"Markdown files are managed natively" }
            li { +"Any document can be exported to PDF following predefined visual identity templates" }
            li { +"Any git project can be plugged in as a part of the workspace" }
            li { +"Changes to files proposed by Golem are presented in IntelliJ like parallel split view" }
        }
    }
}
