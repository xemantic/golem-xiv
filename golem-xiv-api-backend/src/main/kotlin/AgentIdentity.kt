/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.backend

interface AgentIdentity {

    suspend fun getSelfId(): Long

    // TODO should be something like external user id, since login can change
    suspend fun getUserId(login: String): Long

    suspend fun getComputerId(): Long

}
