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
import kotlin.time.Instant

@Serializable
sealed interface EpistemicAgent {

    val id: Long

    @Serializable
    data class AI(
        override val id: Long,
        val model: String,
        val vendor: String
    ) : EpistemicAgent

    @Serializable
    data class Human(
        override val id: Long
    ) : EpistemicAgent

    // TODO create connection between human/user and their computer
    @Serializable
    data class Computer(
        override val id: Long,
    ) : EpistemicAgent

}

@Serializable
data class PhenomenalExpression(
    val id: Long,
    val agent: EpistemicAgent,
    val phenomena: List<Phenomenon>,
    val initiationMoment: Instant,
    val culminationMoment: Instant? = null
)

@Serializable
sealed interface Phenomenon {

    val id: Long

    @Serializable
    @SerialName("Text")
    data class Text(
        override val id: Long,
        val text: String
    ) : Phenomenon

    @Serializable
    @SerialName("Image")
    data class Image(
        override val id: Long,
        val uri: String
    ) : Phenomenon

    @Serializable
    @SerialName("Document")
    data class Document(
        override val id: Long,
        val uri: String
    ) : Phenomenon

    @Serializable
    @SerialName("Intent")
    data class Intent(
        override val id: Long,
        val systemId: String,
        val purpose: String,
        val code: String
    ) : Phenomenon

    @Serializable
    @SerialName("Fulfillment")
    data class Fulfillment(
        override val id: Long,
        val intentId: Long,
        val intentSystemId: String,
        val result: String,
        val impeded: Boolean = false
    ) : Phenomenon

}

@Serializable
data class CognitionListItem(
    val id: Long,
    val title: String?,
    val initiationMoment: Instant
)
