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

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

/**
 * Test utility for managing DDGS Docker container in integration tests.
 */
object TestDdgs {

    private const val DDGS_PORT = 8000

    /**
     * Docker image to use for tests.
     * Uses local image when available, falls back to GitHub Container Registry image in CI.
     */
    private val dockerImage: String = System.getenv("CI")?.let {
        "ghcr.io/xemantic/ddgs:latest"
    } ?: "ddgs:latest"

    private val container: GenericContainer<*> by lazy {
        GenericContainer(DockerImageName.parse(dockerImage))
            .withExposedPorts(DDGS_PORT)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .apply {
                start()
            }
    }

    val baseUrl: String by lazy {
        "http://${container.host}:${container.getMappedPort(DDGS_PORT)}"
    }

    fun stop() {
        container.stop()
    }
}
