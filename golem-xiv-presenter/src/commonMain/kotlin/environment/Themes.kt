/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.environment

enum class Theme {

    LIGHT,
    DARK;

    fun opposite() = when (this) {
        LIGHT -> DARK
        DARK -> LIGHT
    }

}

interface ThemeManager {

    var theme: Theme

}

interface DefaultThemeProvider {

    val defaultTheme: Theme

}

class LocalStorageThemeManager(
    private val localStorage: LocalStorage,
    defaultTheme: Theme
) : ThemeManager {

    private var currentTheme = localStorage.getItem(
        key = "theme"
    )?.let {
        try {
            Theme.valueOf(it)
        } catch (e : IllegalArgumentException) {
            // TODO logger here
            null
        }
    } ?: defaultTheme

    override var theme: Theme
        get() = currentTheme
        set(value) {
            currentTheme = value
            localStorage.setItem("theme", value.name)
        }

}
