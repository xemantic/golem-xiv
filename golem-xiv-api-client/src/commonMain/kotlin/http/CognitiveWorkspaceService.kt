/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.client.http

import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.client.CognitiveWorkspaceService
import io.ktor.client.HttpClient

class HttpClientCognitiveWorkspaceService(
    private val client: HttpClient
) : CognitiveWorkspaceService {

    override suspend fun initiate(
        phenomena: List<Phenomenon>
    ): Long = client.servicePut(
        uri = "/api/workspaces",
        value = phenomena
    )

    override suspend fun integrate(
        workspaceId: Long,
        phenomena: List<Phenomenon>
    ) {
        client.servicePatch(
            uri = "/api/workspaces/$workspaceId",
            value = phenomena
        )
    }

}
