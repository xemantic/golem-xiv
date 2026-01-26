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

package com.xemantic.ai.golem.core.script.service

import com.xemantic.ai.golem.api.backend.script.Http
import com.xemantic.ai.golem.api.backend.script.MarkdownContentType
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class KtorHttp : Http, AutoCloseable {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    override suspend fun get(
        url: String,
        accept: ContentType
    ): HttpResponse {
        val effectiveUrl = when {

            accept.match(MarkdownContentType) -> {
                val contentType = client.head(url).contentType()
                if (contentType != null) {
                    if (contentType.match(MarkdownContentType)
                        || contentType.match(ContentType.Application.Json)) {
                        url
                    } else {
                        "https://r.jina.ai/$url"
                    }
                } else {
                    "https://r.jina.ai/$url"
                }
            }

            accept.match(ContentType.Application.Json) -> url

            else -> url

        }
        return client.get(effectiveUrl)
    }

    override fun close() {
        client.close()
    }

}
