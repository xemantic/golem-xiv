/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.script.service

import com.xemantic.ai.golem.api.backend.script.Http
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

class KtorHttp : Http {

    override fun client(
        block: HttpClientConfig<*>.() -> Unit
    ): HttpClient = HttpClient(block)

}
