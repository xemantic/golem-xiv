package com.xemantic.ai.golem.cognizer.anthropic

import com.xemantic.ai.golem.api.CognitionEvent
import com.xemantic.ai.golem.api.backend.CognizerEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

class CognitiveProcessor(
    cognitionBus: FlowCollector<CognitionEvent>
) {

    suspend fun process(events: List<CognizerEvent>): CognitiveContinuation {
        return CognitiveContinuation("", "")

    }

}

data class CognitiveContinuation(
    val text: String,
    val intent: String?
)
