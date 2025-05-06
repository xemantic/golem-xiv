/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.service

import com.xemantic.ai.golem.api.ContextInfo
import com.xemantic.ai.golem.api.Prompt
import com.xemantic.ai.golem.api.serviceGet
import com.xemantic.ai.golem.api.servicePatch
import com.xemantic.ai.golem.api.servicePut
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class ClientContextService(
    private val client: HttpClient
) : ContextService {

    override suspend fun start(
        prompt: Prompt
    ): ContextInfo = client.servicePut("/api/contexts", prompt)

    override suspend fun append(
        contextId: Uuid,
        prompt: Prompt
    ) {
        client.servicePatch("/api/contexts/$contextId", prompt)
    }

    override suspend fun get(
        contextId: Uuid
    ): ContextInfo? = client.serviceGet("/api/contexts/$contextId")

    override fun list(): Flow<ContextInfo> {
        TODO("Not yet implemented")
    }

}
