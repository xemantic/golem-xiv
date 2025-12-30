/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025  Kazimierz Pogoda / Xemantic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.xemantic.ai.golem.api.client

import com.xemantic.ai.golem.api.GolemError
import com.xemantic.ai.golem.api.Phenomenon

interface PingService {

    suspend fun ping(): String

}

interface CognitionService {

    suspend fun initiateCognition(
        phenomena: List<Phenomenon>
    ): Long

    /**
     * Emits a cognition instance via WebSocket.
     *
     * @param id the cognition id
     * @throws GolemServiceException if no such cognition exists.
     */
    suspend fun emitCognition(
        id: Long
    )

    /**
     * @throws GolemServiceException if no such cognition exists.
     */
    suspend fun continueCognition(
        id: Long,
        phenomena: List<Phenomenon>
    )

}

// TODO should be rather exception hierarchy
class GolemServiceException(
    val uri: String,
    val error: GolemError
) : RuntimeException("Golem API error: $uri - $error")
