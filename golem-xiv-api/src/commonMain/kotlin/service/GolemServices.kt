/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.service

import com.xemantic.ai.golem.api.Expression
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.CognitiveWorkspace

interface PingService {

    suspend fun ping(): String

}

interface CognitiveWorkspaceService {

    /**
     * Initiates the cognition.
     *
     * 1. The new [CognitiveWorkspace] will be created.
     * 2. The stream of [phenomena] will be:
     *   - turned into [Expression]
     *   - integrated into the workspace as initial trigger
     * 3. The cognition will start by handing the [CognitiveWorkspace] to the [Cognizer].
     *
     * @param phenomena the list of phenomena to cognize.
     * @return the id of newly created cognitive workspace.
     */
    suspend fun initiate(
        phenomena: List<Phenomenon>
    ): String

    /**
     * Integrates a new expression into an existing cognitive workspace.
     *
     * This method represents the process of incorporating new cognitive elements (provided as a list of [Phenomenon])
     * into the active cognitive process. The integration may trigger further cognitive processing, including:
     *
     * 1. Recognition and processing of new phenomena within the expression
     * 2. Formation of connections between existing and new cognitive elements
     * 3. Possible emergence of new intents, fulfillments, or impediments
     *
     * @param workspaceId The identifier of the cognitive workspace to integrate the expression into
     * @param expression The [Expression] to be integrated into the cognitive workspace
     * @throws WorkspaceNotFoundException If no cognitive workspace exists with the provided ID
     * @throws InvalidExpressionException If the expression cannot be properly integrated (e.g., malformed)
     */
    // TODO implement these
    suspend fun integrate(
        workspaceId: String,
        phenomena: List<Phenomenon>
    )

}

class GolemServiceException(
    uri: String,
    message: String
) : RuntimeException("Golem API error: $uri - $message")
