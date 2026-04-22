/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
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

package com.xemantic.golem.viewmodel.navigation

data class NavigationStrings(
    val cognition: String,
    val cognitionAccessibilityLabel: String,
    val workspace: String,
    val workspaceAccessibilityLabel: String,
    val memory: String,
    val memoryAccessibilityLabel: String,
    val solicitations: String,
    val solicitationsAccessibilityLabel: String,
    val computers: String,
    val computersAccessibilityLabel: String,
    val settings: String,
    val settingsAccessibilityLabel: String,

    val mainNavigationAccessibilityLabel: String,
    val mainMenuAccessibilityLabel: String,

    val lightMode: String,
    val darkMode: String,
    val themeSwitcherAccessibilityLabel: String,

    val searchCognitions: String
) {

    fun labelOf(
        target: Navigation.Target
    ): String = when (target) {
        is Cognition -> cognition
        is Workspace -> workspace
        is Memory -> memory
        is Solicitations -> solicitations
        is Computers -> computers
        is Settings -> settings
        else -> throw IllegalArgumentException("No label found for $target")
    }

    fun accessibilityLabelOf(
        target: Navigation.Target
    ): String = when (target) {
        is Cognition -> cognitionAccessibilityLabel
        is Workspace -> workspaceAccessibilityLabel
        is Computers -> computersAccessibilityLabel
        is Memory -> memoryAccessibilityLabel
        is Solicitations -> solicitationsAccessibilityLabel
        is Settings -> settingsAccessibilityLabel
        else -> throw IllegalArgumentException("No accessibility label found for $target")
    }

}
