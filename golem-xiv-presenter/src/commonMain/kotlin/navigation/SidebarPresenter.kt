/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.navigation

import com.xemantic.ai.golem.presenter.Theme
import com.xemantic.ai.golem.presenter.util.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface SidebarView {

    val themeChanges: Flow<Theme>

    val resizes: Flow<Action>

    fun theme(theme: Theme)

    var opened: Boolean

}

class SidebarPresenter(
    scope: CoroutineScope,
    view: SidebarView,
    toggles: Flow<Action>
) {

    var opened: Boolean = false

    init {
        scope.launch {
            toggles.collect {
                println("toggle collected1")
                opened = !opened
                view.opened = opened
            }
        }
        scope.launch {
            view.resizes.collect {
                opened = false
                view.opened = opened
            }
        }
        scope.launch {
            view.themeChanges.collect {
                view.theme(it)
            }
        }
    }

}
