/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.navigation

import com.xemantic.ai.golem.presenter.Theme
import com.xemantic.ai.golem.presenter.util.Action
import com.xemantic.ai.golem.presenter.util.ScopedPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

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
) : ScopedPresenter(scope) {

    var opened: Boolean = false

    init {
        toggles.listen {
            opened = !opened
            view.opened = opened
        }
        view.resizes.listen {
            opened = false
            view.opened = opened
        }
        view.themeChanges.listen {
            view.theme(it)
        }
        view.memoryActions.listen {
            navigation.navigate(Navigation.Target.KnowledgeGraph)
        }
    }

}
