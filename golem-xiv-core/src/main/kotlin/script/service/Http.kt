/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core.script.service

import com.xemantic.ai.golem.api.backend.script.Http
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

class KtorHttp : Http, AutoCloseable {

    private val client = HttpClient {
//        install(ContentNegotiation) {
//            json()
//        }
    }

    override suspend fun get(url: String): HttpResponse {
        return client.get(url)
    }

    override fun close() {
        client.close()
    }

}
