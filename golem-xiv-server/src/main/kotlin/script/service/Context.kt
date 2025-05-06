/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script.service

import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.server.script.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

class DefaultContext(
    private val scope: CoroutineScope,
    private val outputs: FlowCollector<GolemOutput>
) : Context {

    override var title: String = "Untitled"
        get() = field
        set(value) {
            field = value
            scope.launch {
//                outputs.emit(value)
            }
        }

}
