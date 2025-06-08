/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.environment

import com.xemantic.ai.golem.presenter.environment.DefaultThemeProvider
import com.xemantic.ai.golem.presenter.environment.Theme

class BrowserDefaultThemeProvider() : DefaultThemeProvider {

    override val defaultTheme: Theme get() = themeFromColorScheme(
        scheme = preferredColorScheme()
    )

}
