/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright

private val playwright = Playwright.create()

private val browser by lazy {
    playwright.chromium().launch(
        BrowserType.LaunchOptions().setHeadless(false)
    )!!
}
