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

package com.xemantic.ai.golem.web.util

import org.w3c.dom.HTMLElement

fun <T : HTMLElement> T.inject(
    vararg injections: Pair<HTMLElement, String>,
): T {
    injections.forEach { (element, selector) ->
        querySelector(selector)?.append(element) ?: throw IllegalArgumentException(
            "Selector '$selector' did not match any element."
        )
    }
    return this
}

fun <T : HTMLElement> T.children(
    vararg elements: HTMLElement
): HTMLElement = also {
    elements.forEach {
        append(it)
    }
}


fun <T : HTMLElement> T.appendTo(
    selector: String,
    vararg elements: HTMLElement
): HTMLElement = also {
    querySelector(selector)?.run {
        elements.forEach {
            this@run.append(it)
        }
    } ?: throw IllegalArgumentException(
        "Selector '$selector' did not match any element."
    )
}
