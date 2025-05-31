/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.backend.api

import com.xemantic.ai.golem.api.PhenomenalExpression
import com.xemantic.ai.golem.api.CognitionEvent
import kotlinx.coroutines.flow.Flow

interface Cognizer {

    fun reason(
        conditioning: List<String>,
        workspaceId: Long,
        phenomenalFlow: List<PhenomenalExpression>,
        hints: Map<String, String>
    ): Flow<CognitionEvent>

}
