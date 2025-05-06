/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.service

import com.xemantic.ai.golem.api.serviceGet
import io.ktor.client.HttpClient

class ClientPingService(
    private val client: HttpClient
) : PingService {

    override suspend fun ping() = client.serviceGet<String>("/api/ping")

}
