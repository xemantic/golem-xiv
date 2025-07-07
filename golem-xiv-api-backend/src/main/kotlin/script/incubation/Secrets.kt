/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.backend.script.incubation

import kotlinx.coroutines.flow.Flow

interface Secrets {
    suspend fun save(key: String, value: String)
    suspend fun get(key: String): String
    /** Instead of returning all keys, try to find matching ones. */
    fun listKeys(): Flow<String>
}
