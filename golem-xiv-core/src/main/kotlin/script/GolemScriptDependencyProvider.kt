/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.script

import com.xemantic.ai.golem.api.backend.CognitionRepository
import com.xemantic.ai.golem.api.backend.script.Memory
import com.xemantic.ai.golem.core.script.service.ActualMind
import com.xemantic.ai.golem.core.script.service.KtorHttp
import com.xemantic.ai.golem.core.script.service.LocalFiles

class GolemScriptDependencyProvider(
    private val repository: CognitionRepository,
    private val memoryProvider: (cognitionId: Long, fulfillmentId: Long) -> Memory
) {

    private val files = LocalFiles()

    private val http = KtorHttp()

    fun dependencies(
        cognitionId: Long,
        fulfillmentId: Long
    ): List<GolemScriptExecutor.Dependency<*>> {
        return listOf(
            service("mind", ActualMind(repository, cognitionId)),
            service("memory", memoryProvider(cognitionId, fulfillmentId)),
            service("files", files),
            service("http", http)
        )
    }

}


private inline fun <reified T : Any> service(
    name: String,
    value: T
): GolemScriptExecutor.Dependency<T> = GolemScriptExecutor.Dependency(
    name,
    T::class,
    value
)
