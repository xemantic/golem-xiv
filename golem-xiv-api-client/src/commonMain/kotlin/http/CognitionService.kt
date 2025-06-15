/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.client.http

import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.client.CognitionService
import io.ktor.client.HttpClient

class HttpClientCognitionService(
    private val client: HttpClient
) : CognitionService {

    override suspend fun initiateCognition(
        phenomena: List<Phenomenon>
    ): Long = client.servicePut(
        uri = "/api/cognitions",
        value = phenomena
    )

    override suspend fun emitCognition(id: Long) {
        client.serviceGet<String>("/api/cognitions/$id")
    }

    override suspend fun continueCognition(
        id: Long,
        phenomena: List<Phenomenon>
    ) {
        client.servicePatch(
            uri = "/api/cognitions/$id",
            value = phenomena
        )
    }

}
