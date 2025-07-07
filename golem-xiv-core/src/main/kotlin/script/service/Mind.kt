/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.script.service

import com.xemantic.ai.golem.api.backend.CognitionRepository
import com.xemantic.ai.golem.api.backend.script.Cognition
import com.xemantic.ai.golem.api.backend.script.Mind

class ActualMind(
    private val repository: CognitionRepository,
    private val cognitionId: Long
) : Mind {
    override suspend fun currentCognition(): Cognition = repository.getCognition(cognitionId)
    override suspend fun getCognition(id: Long): Cognition = repository.getCognition(id)
}
