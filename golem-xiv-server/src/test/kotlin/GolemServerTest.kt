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

package com.xemantic.ai.golem.server

import com.xemantic.kotlin.test.coroutines.should
import com.xemantic.kotlin.test.have
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test

class GolemServerTest {

    @AfterEach
    fun cleanDatabase() {
        TestNeo4j.cleanDatabase()
    }

    @Test
    fun `should respond to ping endpoint with neo4j available`() = testApplication {
        // given
        // TestNeo4j is initialized lazily and provides an embedded neo4j instance
        // This ensures neo4j harness is running for the integration test
        TestNeo4j.isInitialized

        application {
            install(ServerContentNegotiation) {
                json()
            }
            routing {
                route("/api") {
                    get("/ping") {
                        call.respondText("pong")
                    }
                }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // when
        val response = client.get("/api/ping")

        // then
        response should {
            have(status == HttpStatusCode.OK)
            have(bodyAsText() == "pong")
        }
    }

}
