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

package com.xemantic.ai.golem.server

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.server.application.call
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.header
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.utils.io.copyTo

/**
 * Configures a reverse proxy for Neo4j HTTP API.
 *
 * This allows the Neo4j Browser to connect to the Neo4j database through the Golem server,
 * enabling cloud deployments where direct database access is not available.
 *
 * @param neo4jHttpUri The URI of the Neo4j HTTP endpoint (e.g., "http://localhost:7474")
 */
fun Route.neo4jProxy(neo4jHttpUri: String) {
    val httpClient = HttpClient()

    route("/neo4j/{path...}") {
        handle {
            val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
            val queryString = call.request.queryParameters.entries()
                .joinToString("&") { (key, values) ->
                    values.joinToString("&") { value -> "$key=$value" }
                }

            val targetUrl = buildString {
                append(neo4jHttpUri.removeSuffix("/"))
                append("/")
                append(path)
                if (queryString.isNotEmpty()) {
                    append("?")
                    append(queryString)
                }
            }

            logger.debug { "Proxying ${call.request.local.method.value} request to: $targetUrl" }

            val response = httpClient.request(targetUrl) {
                method = call.request.local.method

                // Forward relevant headers
                call.request.headers.forEach { key, values ->
                    when (key) {
                        HttpHeaders.Host,
                        HttpHeaders.ContentLength,
                        HttpHeaders.TransferEncoding -> {
                            // Skip these headers as they will be set by the client
                        }
                        else -> {
                            values.forEach { value ->
                                header(key, value)
                            }
                        }
                    }
                }

                // Forward request body for POST/PUT/PATCH requests
                val contentType = call.request.headers[HttpHeaders.ContentType]
                if (contentType != null) {
                    this.contentType(ContentType.parse(contentType))
                    setBody(call.receiveChannel())
                }
            }

            // Forward response status
            call.response.status(response.status)

            // Forward response headers
            response.headers.forEach { key, values ->
                when (key) {
                    HttpHeaders.TransferEncoding,
                    HttpHeaders.ContentLength -> {
                        // Skip these headers as they will be set automatically
                    }
                    else -> {
                        values.forEach { value ->
                            call.response.header(key, value)
                        }
                    }
                }
            }

            // Stream response body
            call.respondBytesWriter {
                response.bodyAsChannel().copyTo(this)
            }
        }
    }
}
