/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

// TODO remove with kotlin 2.2
@file:UseSerializers(InstantSerializer::class)

package com.xemantic.ai.golem.api

import com.xemantic.ai.golem.serialization.time.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

interface CognitiveWorkspace {
    val id: Long
    var title: String
    var summary: String
    val expressions: List<PhenomenalExpression>
    val initiationMoment: Instant
    val latestUpdateMoment: Instant
    val active: Boolean
    val parentId: String?
}

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

    @Serializable
    data class Computer(
        override val id: Long,
        val belongsToAgentId: Long
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
    @SerialName("text")
    data class Text(
        override val id: Long,
        val text: String
    ) : Phenomenon

    @Serializable
    @SerialName("image")
    data class Image(
        override val id: Long,
        val uri: String
    ) : Phenomenon

    @Serializable
    @SerialName("document")
    data class Document(
        override val id: Long,
        val uri: String
    ) : Phenomenon

    @Serializable
    @SerialName("intent")
    data class Intent(
        override val id: Long,
        val systemId: String,
        val purpose: String,
        val code: String
    ) : Phenomenon

    @Serializable
    @SerialName("fulfillment")
    data class Fulfillment(
        override val id: Long,
        val intentId: String,
        val intentSystemId: String,
        val result: String
    ) : Phenomenon

    @Serializable
    @SerialName("impediment")
    data class Impediment(
        override val id: Long,
        val intentId: String,  // Reference to the original intent
        val intentSystemId: String,
        val reason: String,    // Why the intent failed
    ) : Phenomenon

}
