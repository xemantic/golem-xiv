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

package com.xemantic.ai.golem.web.js

import kotlinx.html.FlowContent
import kotlinx.html.HtmlTagMarker
import kotlinx.html.I
import kotlinx.html.TagConsumer
import kotlinx.html.i
import kotlin.contracts.ExperimentalContracts

@HtmlTagMarker
@OptIn(ExperimentalContracts::class)
inline fun FlowContent.icon(name: String) {
    i("fas fa-$name")
}

inline fun TagConsumer<*>.icon(
    name: String
): I = i("fas fa-$name") as I
