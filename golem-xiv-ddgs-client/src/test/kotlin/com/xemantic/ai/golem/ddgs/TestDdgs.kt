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

package com.xemantic.ai.golem.ddgs

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

/**
 * Test utility for managing DDGS Docker container in integration tests.
 */
object TestDdgs {

    private const val DDGS_PORT = 8000

    private val container: GenericContainer<*> by lazy {
        GenericContainer(DockerImageName.parse("deedy5/ddgs:latest"))
            .withExposedPorts(DDGS_PORT)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .apply {
                start()
            }
    }

    val baseUrl: String by lazy {
        "http://${container.host}:${container.getMappedPort(DDGS_PORT)}"
    }

    val httpClient: HttpClient by lazy {
        HttpClient(Java) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
        }
    }

    val client: DdgsClient by lazy {
        DdgsClient(
            baseUrl = baseUrl,
            httpClient = httpClient
        )
    }

    fun stop() {
        container.stop()
        httpClient.close()
    }
}
