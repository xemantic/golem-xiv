/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface GolemError {

    @Serializable
    @SerialName("NoSuchCognition")
    data class NoSuchCognition(
        val cognitionId: Long
    ) : GolemError

    @Serializable
    @SerialName("BadRequest")
    data class BadRequest(
        val message: String
    ) : GolemError

    @Serializable
    @SerialName("Unexpected")
    data class Unexpected(
        val message: String
    ) : GolemError

}
