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

package com.xemantic.ai.golem.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface WithCognitionId {

    val cognitionId: Long

}

@Serializable
sealed interface GolemOutput {

    @Serializable
    @SerialName("Welcome")
    data class Welcome (
        val message: String
    ): GolemOutput

    @Serializable
    @SerialName("Message")
    data class Message (
        override val cognitionId: Long,
        val expression: PhenomenalExpression
    ): GolemOutput, WithCognitionId

    @Serializable
    @SerialName("CognitionAdded")
    data class CognitionAdded(
        override val cognitionId: Long
    ) : GolemOutput, WithCognitionId

    @Serializable
    @SerialName("CognitionUpdated")
    data class CognitionUpdated(
        override val cognitionId: Long,
//        val info: CognitionInfo
    ) : GolemOutput, WithCognitionId

    @Serializable
    @SerialName("Cognition")
    data class Cognition(
        override val cognitionId: Long,
        val event: CognitionEvent
    ) : GolemOutput, WithCognitionId

}
