/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.ai.golem.api.service

import com.xemantic.ai.golem.api.ContextInfo
import com.xemantic.ai.golem.api.Prompt
import com.xemantic.ai.golem.api.serviceGet
import com.xemantic.ai.golem.api.servicePatch
import com.xemantic.ai.golem.api.servicePut
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

class ClientContextService(
    private val client: HttpClient
) : ContextService {

    override suspend fun start(
        prompt: Prompt
    ): ContextInfo = client.servicePut("/api/contexts", prompt)

    override suspend fun append(
        contextId: Uuid,
        prompt: Prompt
    ) {
        client.servicePatch("/api/contexts/$contextId", prompt)
    }

    override suspend fun get(
        contextId: Uuid
    ): ContextInfo? = client.serviceGet("/api/contexts/$contextId")

    override fun list(): Flow<ContextInfo> {
        TODO("Not yet implemented")
    }

}
