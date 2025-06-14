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
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

interface Cognition {
    val id: Long
    val initiationMoment: Instant
    val parentId: Long?
    suspend fun getTitle(): String?
    suspend fun setTitle(title: String?)
    suspend fun getSummary(): String?
    suspend fun setSummary(summary: String?)
    fun expressions(): Flow<PhenomenalExpression>

    enum class State {
        OPEN,
        INTERACTION_PENDING,
        CONCLUDED
    }

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
