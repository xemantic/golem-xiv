/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
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

package com.xemantic.ai.golem.core.cognition

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.CognitionRepository
import com.xemantic.ai.golem.core.golemMainConstitution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class CognitiveOrchestrator(
    private val repository: CognitionRepository
) {

    private val activeCognitions = ConcurrentHashMap<Long, CognitionEntry>()

    init {
        //repository.listActiveCognitions()
    }

    /**
     * Dispatches to [com.xemantic.ai.golem.api.backend.Cognizer]
     * and broadcasts the
     */
    fun perceive(
        cognitionId: Long? = null,
        phenomena: List<Phenomenon>,
        epistemicAgentId: Long
    ) {
        require(phenomena.isNotEmpty()) {
            "phenomena must not be empty"
        }

        val entry = if (cognitionId == null) {
            val cognitionInfo = repository.initiateCognition(
                constitution = listOf(golemMainConstitution)
            )
            CognitionEntry(cognitionId)

//            val cognitionEntry = activeCognitions.computeIfAbsent(cognitionInfo.id) {
//            }
        } else {
            activeCognitions[cognitionInfo.id] = CognitionEntry(cognitionInfo.id)

            CognitionEntry()
        }

        entry.scope
    }

    /**
     * Called by SSE clients subscribing for updates
     */
    fun follow(
        cognitionId: Long
    ): Flow<CognitionEvent> {
        activeCognitions.computeIfAbsent(cognitionId) {
            val cognition = repository.getCognition(cognitionId)
        }
    }
        activeCognitions[cognitionId]?.flow ?: throw IllegalArgumentException(
            "No active cognition found for $cognitionId"
        )

//    fun activeCognitions(): List<CognitionInfo> {
//
//    }

}

private class CognitionEntry(
    val cognitionId: Long
) {
    val flow: MutableSharedFlow<CognitionEvent> = MutableSharedFlow(
        replay = Int.MAX_VALUE
    )
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
}
