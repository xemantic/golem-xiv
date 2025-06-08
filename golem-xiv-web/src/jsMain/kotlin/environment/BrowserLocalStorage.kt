/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.environment

import com.xemantic.ai.golem.presenter.environment.LocalStorage
import kotlinx.browser.localStorage

class BrowserLocalStorage : LocalStorage {

    override fun getItem(
        key: String
    ): String? = localStorage.getItem(key)

    override fun setItem(key: String, value: String) {
        localStorage.setItem(key,value)
    }

}
