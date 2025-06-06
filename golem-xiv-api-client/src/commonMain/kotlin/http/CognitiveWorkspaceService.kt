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
        uri = "/api/workspaces",
        value = phenomena
    )

    override suspend fun continueCognition(
        workspaceId: Long,
        phenomena: List<Phenomenon>
    ) {
        client.servicePatch(
            uri = "/api/workspaces/$workspaceId",
            value = phenomena
        )
    }

}
