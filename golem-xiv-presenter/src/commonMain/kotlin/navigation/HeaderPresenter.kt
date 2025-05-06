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
import kotlinx.coroutines.launch

interface HeaderView {

    val toggleMenuClicks: Flow<Action>

}

class HeaderPresenter(
    scope: CoroutineScope,
    view: HeaderView,
    menuToggleHandler: suspend () -> Unit
) {

    init {
        scope.launch {
            view.toggleMenuClicks.collect {
                menuToggleHandler()
            }
        }
    }

}
