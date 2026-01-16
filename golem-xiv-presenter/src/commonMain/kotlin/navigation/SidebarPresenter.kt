/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.golem.presenter.navigation

import com.xemantic.ai.golem.api.CognitionListItem
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.client.CognitionService
import com.xemantic.ai.golem.presenter.environment.Theme
import com.xemantic.ai.golem.presenter.util.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface SidebarView {

    val initiateCognitionActions: Flow<Action>

    val memoryActions: Flow<Action>

    val themeChanges: Flow<Action>

    val resizes: Flow<Action>

    val cognitionSelections: Flow<Long>

    fun themeActionLabel(theme: Theme)

    fun updateCognitionList(cognitions: List<CognitionListItem>)

    var opened: Boolean

}

class SidebarPresenter(
    scope: CoroutineScope,
    private val view: SidebarView,
    toggles: Flow<Action>,
    navigation: Navigation,
    themeChangesSink: FlowCollector<Theme>,
    private val cognitionService: CognitionService,
    golemOutputs: Flow<GolemOutput>
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

        view.cognitionSelections.onEach { cognitionId ->
            navigation.navigateTo(Navigation.Target.Cognition(cognitionId))
        }.launchIn(scope)

        // Load initial cognition list
        scope.launch {
            refreshCognitionList()
        }

        // Refresh on title updates
        golemOutputs.filterIsInstance<GolemOutput.CognitionTitleUpdated>().onEach {
            refreshCognitionList()
        }.launchIn(scope)

        // Refresh on new cognitions
        golemOutputs.filterIsInstance<GolemOutput.CognitionAdded>().onEach {
            refreshCognitionList()
        }.launchIn(scope)

    }

    private suspend fun refreshCognitionList() {
        val cognitions = cognitionService.listCognitions()
        view.updateCognitionList(cognitions)
    }

}
