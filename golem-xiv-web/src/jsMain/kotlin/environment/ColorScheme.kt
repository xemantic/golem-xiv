/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.environment

import com.xemantic.ai.golem.presenter.environment.Theme
import kotlinx.browser.window

fun preferredColorScheme(): String? = if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
    "dark"
} else if (window.matchMedia("(prefers-color-scheme: light)").matches) {
    "light"
} else {
    null
}

fun themeFromColorScheme(
    scheme: String?
): Theme = when (scheme) {
    "dark" -> Theme.DARK
    "light" -> Theme.LIGHT
    else -> Theme.LIGHT
}
