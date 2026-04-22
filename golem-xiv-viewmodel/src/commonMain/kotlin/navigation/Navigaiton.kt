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

import com.xemantic.ai.golem.api.golemJson
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.*

interface Navigation {

    suspend fun navigateTo(target: Target)

    val activeTarget: StateFlow<Target>

    @Serializable
    sealed class Target(
        @Transient
        val icon: String = ""
    ) {

        @OptIn(InternalSerializationApi::class)
        @Contextual
        val name: String get() = this::class.serializer().descriptor.serialName

        @OptIn(InternalSerializationApi::class)
        fun toJson(): String = golemJson.encodeToString<Target>(this)

        @Serializable
        @SerialName("cognition")
        data class Cognition(val cogitationId: Long? = null) : Target(icon = "network_intel_node") {
        }

        @Serializable
        @SerialName("workspace")
        data class Workspace(val path: String? = null) : Target(icon = "construction") {
        }

        @Serializable
        @SerialName("memory")
        object Memory : Target(icon = "graph_3") {
        }

        @Serializable
        @SerialName("solicitations")
        object Solicitations : Target(icon = "sensors") {
        }

        @Serializable
        @SerialName("computers")
        data class Computers(val id: Long? = null) : Target(icon = "desktop_cloud_stack") {
        }

        @Serializable
        @SerialName("settings")
        object Settings : Target(icon = "settings") {
        }

        @Serializable
        @SerialName("notFound")
        data class NotFound(
            val message: String,
            val pathname: String
        ) : Target(icon = "N/A") {
        }

    }

}

/**
 * Parses navigation targets, it is using web pathname, but addressing is independent, from the rendering tech
 */
fun Navigation.Target.Companion.parse(
    pathname: String
): Navigation.Target {

    val split = pathname.removePrefix("/").split("/").filter {
        it.isNotEmpty()
    }
    return when (split.size) {
        0 -> Navigation.Target.Cognition()
        1 -> when (split[0]) {
            "workspace" -> Navigation.Target.Workspace()
            "memory" -> Navigation.Target.Memory
            "solicitations" -> Navigation.Target.Solicitations
            "computers" -> Navigation.Target.Computers()
            "settings" -> Navigation.Target.Settings
            else -> Navigation.Target.NotFound(
                message = "No such path: $pathname",
                pathname = pathname
            )
        }
        else -> when (split[0]) {
            "cognition" -> try {
                val id = split[1].toLong()
                Navigation.Target.Cognition(id)
            } catch (_: NumberFormatException) {
                Navigation.Target.NotFound(
                    message = "Invalid cognition id (must be a number): ${split[1]}",
                    pathname = pathname
                )
            }
            "workspace" -> Navigation.Target.Workspace(
                path = "/" + split.drop(1).joinToString("/")
            )
            else -> Navigation.Target.NotFound(
                message = "No such path: $pathname",
                pathname = pathname
            )
        }
    }

}

fun Navigation.Target.Companion.fromJson(
    json: String
): Navigation.Target = golemJson.decodeFromString<Navigation.Target>(json)
