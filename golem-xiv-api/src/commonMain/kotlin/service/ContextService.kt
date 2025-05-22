/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.service

import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.servicePatch
import com.xemantic.ai.golem.api.servicePut
import io.ktor.client.HttpClient

class ClientCognitiveWorkspaceService(
    private val client: HttpClient
) : CognitiveWorkspaceService {

    override suspend fun initiate(
        phenomena: List<Phenomenon>
    ): String = client.servicePut(
        uri = "/api/workspaces",
        value = phenomena
    )

    override suspend fun integrate(
        workspaceId: String,
        phenomena: List<Phenomenon>
    ) {
        client.servicePatch(
            uri = "/api/workspaces/$workspaceId",
            value = phenomena
        )
    }

}
