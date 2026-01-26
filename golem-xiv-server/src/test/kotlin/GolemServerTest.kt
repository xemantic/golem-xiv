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
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.Test

class GolemServerTest {

    // just a bootstrap of server tests, needs to be expanded, see xemantic-neo4j-demo for inspiration
    @Test
    fun `should check health endpoint status`() = testApplication {
        // given
        application {
            //module()
        }

        client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // when
        val response = client.get("/health")

        // then
        response should {
            have(status == HttpStatusCode.NotFound)
            //have(bodyAsText() == "")
        }
    }

}
