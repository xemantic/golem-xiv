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

package com.xemantic.golem.web.environment

import com.xemantic.golem.viewmodel.environment.LocalStorage
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

class BrowserLocalStorage : LocalStorage {

    override fun get(
        key: String
    ): String? = localStorage[key]

    override fun set(key: String, value: String) {
        localStorage[key] = value
    }

}
