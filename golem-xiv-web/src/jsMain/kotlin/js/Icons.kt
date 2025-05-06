/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.js

import kotlinx.html.FlowContent
import kotlinx.html.I
import kotlinx.html.TagConsumer
import kotlinx.html.i

fun FlowContent.icon(name: String) {
    i("fas fa-$name")
}

fun TagConsumer<*>.icon(
    name: String
): I = i("fas fa-$name") as I
