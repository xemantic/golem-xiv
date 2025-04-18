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

package com.xemantic.ai.golem.server.server

import com.xemantic.ai.golem.api.Content
import com.xemantic.ai.golem.api.ContextInfo
import com.xemantic.ai.golem.api.Prompt
import com.xemantic.ai.golem.server.Golem
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlin.uuid.Uuid

fun Route.golemApiRoute(
    golem: Golem
) {

    get("/ping") {
        call.respondText("pong")
    }

    get("/contexts") {
//        call.respond(
//            golem.contexts
//        )
    }

    put("/contexts") {
        val prompt = call.receive<Prompt>()
        val context = golem.newContext(prompt)
        call.respond(
            ContextInfo(
                id = context.id,
                title = "Untitled" // Can be localized in the future
            )
        )
    }

    patch("/contexts/{id}") {
        val prompt = call.receive<Prompt>()
        val idParameter = requireNotNull(call.parameters["id"]) { "Should never happen" }
        val id = Uuid.parse(idParameter)
        val contex = golem.getContext(id)
        if (contex == null) {
            call.respond(HttpStatusCode.NotFound, "resource not found $")
        } else {
            contex.send(prompt)
        }
    }

    get("/contexts/{id}") {
//        call.respond(
//            golem.contexts
//        )
    }

    post("/contexts") {

    }

}
