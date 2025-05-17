/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

@file:UseSerializers(InstantIso8601Serializer::class)

package com.xemantic.ai.golem.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

/** Cognitive workspace */
interface CognitiveWorkspace {
    val id: String
    val title: String
    val expressions: List<Expression>
    val creationDate: Instant
    val updateDate: Instant
    val parentId: String?
}

/** Cognitive agent */
@Serializable
data class Agent(
    val id: String,
    val description: String,
    val category: Category,
    val model: String? = null,
    val vendor: String? = null
) {

    enum class Category {
        SELF, // it is you!
        HUMAN,
        OTHER_MACHINE
    }

}

/** Phenomenal Expression */
@Serializable
data class Expression(
    val id: String,
    val agent: Agent,
    val phenomena: List<Phenomenon>,
    val initiationMoment: Instant,
    val culminationMoment: Instant? = null // TODO should it be non-null and the whole expression only accessible once it is finished?
)

@Serializable
sealed interface Phenomenon {

    @Serializable
    @SerialName("text")
    data class Text(
        val id: String,
        val text: String
    ) : Phenomenon

    @Serializable
    @SerialName("image")
    data class Image(
        val id: String
    ) : Phenomenon

    @Serializable
    @SerialName("document")
    data class Document(
        val id: String
    ) : Phenomenon

    @Serializable
    @SerialName("intent")
    data class Intent(
        val id: String,
        val systemId: String,
        val purpose: String,
        val instructions: String
    ) : Phenomenon

    @Serializable
    @SerialName("fulfillment")
    data class Fulfillment(
        val id: String,
        val intentId: String,
        val intentSystemId: String,
        val result: String
    ) : Phenomenon

    @Serializable
    @SerialName("impediment")
    data class Impediment(
        val id: String,
        val intentId: String,  // Reference to the original intent
        val intentSystemId: String,
        val reason: String,    // Why the intent failed
    ) : Phenomenon

}

