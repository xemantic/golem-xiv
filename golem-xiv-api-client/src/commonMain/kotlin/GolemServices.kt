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
     * @throws GolemServiceException if no such workspace id exists.
     */
    suspend fun continueCognition(
        workspaceId: Long,
        phenomena: List<Phenomenon>
    )

}

class GolemServiceException(
    uri: String,
    error: GolemError
) : RuntimeException("Golem API error: $uri - $error")
