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

package com.xemantic.ai.golem.core.script

import com.xemantic.ai.golem.api.backend.CognitionRepository
import com.xemantic.ai.golem.api.backend.script.Memory
import com.xemantic.ai.golem.core.script.service.ActualMind
import com.xemantic.ai.golem.core.script.service.KtorHttp
import com.xemantic.ai.golem.core.script.service.LocalFiles
import com.xemantic.ai.golem.kotlin.metadata.DefaultKotlinMetadata

class GolemScriptDependencyProvider(
    private val repository: CognitionRepository,
    private val memoryProvider: (cognitionId: Long, fulfillmentId: Long) -> Memory
) {

    private val files = LocalFiles()

    private val http = KtorHttp()

    private val kotlinMetadata = DefaultKotlinMetadata()

    fun dependencies(
        cognitionId: Long,
        fulfillmentId: Long
    ): List<GolemScriptExecutor.Dependency<*>> {
        return listOf(
            service("mind", ActualMind(repository, cognitionId)),
            service("memory", memoryProvider(cognitionId, fulfillmentId)),
            service("files", files),
            service("http", http),
            service("kotlinMetadata", kotlinMetadata)
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
