/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.service

import com.xemantic.ai.golem.api.ContextInfo
import com.xemantic.ai.golem.api.Prompt
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface PingService {

    suspend fun ping(): String

}

interface ContextService {

    suspend fun start(
        prompt: Prompt
    ): ContextInfo

    suspend fun append(
        contextId: Uuid,
        prompt: Prompt
    )

    suspend fun get(contextId: Uuid): ContextInfo?

    fun list(): Flow<ContextInfo>

}

class GolemServiceException(
    uri: String,
    message: String
) : RuntimeException("Golem API error: $uri - $message")
