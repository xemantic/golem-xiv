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

@Suppress("PropertyName")
data class NavigationStrings(
    val Cognition: String,
    val `View cognition`: String,
    val Workspace: String,
    val `View workspace`: String,
    val Memory: String,
    val `View memory graph`: String,
    val Solicitations: String,
    val `View solicitations`: String,
    val Exchanges: String,
    val `View exchanges`: String,
    val Computers: String,
    val `View computers`: String,
    val Settings: String,
    val `Application settings`: String,

    val `Main navigation`: String,
    val `Main menu`: String,

    val `Light mode`: String,
    val `Dark mode`: String,
    val `Theme switcher`: String,

    val `Search cognitions`: String,

    val `Toggle sidebar menu`: String,
    val Error: String,
) {

    fun labelOf(
        target: Navigation.Target
    ): String = when (target) {
        is Cognition -> Cognition
        is Workspace -> Workspace
        is Memory -> Memory
        is Solicitations -> Solicitations
        is Exchanges -> Exchanges
        is Computers -> Computers
        is Settings -> Settings
        else -> throw IllegalArgumentException("No label found for $target")
    }

    fun accessibilityLabelOf(
        target: Navigation.Target
    ): String = when (target) {
        is Cognition -> `View cognition`
        is Workspace -> `View workspace`
        is Computers -> `View computers`
        is Memory -> `View memory graph`
        is Solicitations -> `View solicitations`
        is Exchanges -> `View exchanges`
        is Settings -> `Application settings`
        else -> throw IllegalArgumentException("No accessibility label found for $target")
    }

}
