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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface SidebarView {

    val themeChanges: Flow<Theme>

    val memoryActions: Flow<Action>

    val resizes: Flow<Action>

    fun theme(theme: Theme)

    var opened: Boolean

}

class SidebarPresenter(
    scope: CoroutineScope,
    view: SidebarView,
    toggles: Flow<Action>,
    navigation: Navigation
) {

    var opened: Boolean = false

    init {

        toggles.onEach {
            opened = !opened
            view.opened = opened
        }.launchIn(scope)

        view.resizes.onEach {
            opened = false
            view.opened = opened
        }.launchIn(scope)

        view.themeChanges.onEach {
            view.theme(it)
        }.launchIn(scope)

        view.memoryActions.onEach {
            navigation.navigate(Navigation.Target.Memory)
        }.launchIn(scope)

    }

}
