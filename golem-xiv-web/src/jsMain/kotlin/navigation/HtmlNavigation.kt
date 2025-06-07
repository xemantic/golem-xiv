/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.navigation

import com.xemantic.ai.golem.presenter.navigation.Navigation
import kotlinx.browser.window
import kotlinx.coroutines.flow.FlowCollector

class HtmlNavigation(
    val navigationTargetSink: FlowCollector<Navigation.Target>
) : Navigation {

    override suspend fun navigate(target: Navigation.Target) {
        when (target) {
            is Navigation.Target.Cognition -> {
                window.history.replaceState(data = null, title = "workspace", url = "/workspaces/${target.id}")
            }
            is Navigation.Target.Memory -> {
                window.history.replaceState(data = null, title = "Golem XIV: Memory", url = "/memory")
            }
        }
        navigationTargetSink.emit(target)
    }

}
