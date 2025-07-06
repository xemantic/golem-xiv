/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server

import kotlinx.serialization.Serializable

@Serializable
data class Neo4jConfig(
    val uri: String,
    val username: String,
    val password: String,
)
