/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.client

import com.xemantic.ai.golem.api.GolemError
import com.xemantic.ai.golem.api.Phenomenon

interface PingService {

    suspend fun ping(): String

}

interface CognitionService {

    suspend fun initiateCognition(
        phenomena: List<Phenomenon>
    ): Long

    /**
     * Emits a cognition instance via WebSocket.
     *
     * @param id the cognition id
     * @throws GolemServiceException if no such cognition exists.
     */
    suspend fun emitCognition(
        id: Long
    )

    /**
     * @throws GolemServiceException if no such cognition exists.
     */
    suspend fun continueCognition(
        id: Long,
        phenomena: List<Phenomenon>
    )

}

// TODO should be rather exception hierarchy
class GolemServiceException(
    val uri: String,
    val error: GolemError
) : RuntimeException("Golem API error: $uri - $error")
