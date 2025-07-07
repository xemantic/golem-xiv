/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.navigation

import com.xemantic.ai.golem.presenter.util.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface HeaderView {

    val toggleMenuClicks: Flow<Action>

}

class HeaderPresenter(
    scope: CoroutineScope,
    view: HeaderView,
    menuToggleHandler: suspend () -> Unit
) {

    init {
        view.toggleMenuClicks.onEach {
            menuToggleHandler()
        }.launchIn(scope)
    }

}
