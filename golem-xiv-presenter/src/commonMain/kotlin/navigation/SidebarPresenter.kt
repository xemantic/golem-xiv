/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.navigation

import com.xemantic.ai.golem.presenter.environment.Theme
import com.xemantic.ai.golem.presenter.util.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface SidebarView {

    val initiateCognitionActions: Flow<Action>

    val memoryActions: Flow<Action>

    val themeChanges: Flow<Action>

    val resizes: Flow<Action>

    fun themeActionLabel(theme: Theme)

    var opened: Boolean

}

class SidebarPresenter(
    scope: CoroutineScope,
    private val view: SidebarView,
    toggles: Flow<Action>,
    navigation: Navigation,
    themeChangesSink: FlowCollector<Theme>
) {

    var opened: Boolean = false

    var theme: Theme = Theme.LIGHT
        get() = field
        set(value) {
            field = value
            view.themeActionLabel(value.opposite())
        }

    init {

        toggles.onEach {
            opened = !opened
            view.opened = opened
        }.launchIn(scope)

        view.initiateCognitionActions.onEach {
            navigation.navigateTo(Navigation.Target.InitiateCognition)
        }.launchIn(scope)

        view.memoryActions.onEach {
            navigation.navigateTo(Navigation.Target.Memory)
        }.launchIn(scope)

        view.themeChanges.onEach {
            theme = theme.opposite()
            themeChangesSink.emit(theme)
        }.launchIn(scope)

        view.resizes.onEach {
            opened = false
            view.opened = opened
        }.launchIn(scope)

    }

}
