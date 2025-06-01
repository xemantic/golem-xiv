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

interface CognitiveWorkspaceService {

    /**
     * Initiates the cognition.
     *
     * 1. The new [com.xemantic.ai.golem.api.CognitiveWorkspace] will be created.
     * 2. The stream of [phenomena] will be:
     *   - turned into [com.xemantic.ai.golem.api.PhenomenalExpression]
     *   - integrated into the workspace as initial trigger
     * 3. The cognition will start by handing the [com.xemantic.ai.golem.api.CognitiveWorkspace] to the [Cognizer].
     *
     * @param phenomena the list of phenomena to cognize.
     * @return the id of newly created cognitive workspace.
     */
    suspend fun initiateCognition(
        phenomena: List<Phenomenon>
    ): Long

    /**
     *
     * @throws GolemServiceException
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
