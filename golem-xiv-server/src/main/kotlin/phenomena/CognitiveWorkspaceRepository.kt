/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.phenomena

import com.xemantic.ai.golem.api.Expression
import com.xemantic.ai.golem.server.CognitiveWorkspace
import kotlin.uuid.Uuid

interface CognitiveWorkspaceRepository {

    fun create(context: CognitiveWorkspace)

    fun update(
        contextId: Uuid,
        expression: Expression
    )

}
