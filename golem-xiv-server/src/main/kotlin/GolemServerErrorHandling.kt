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

import com.xemantic.ai.golem.api.GolemError
import com.xemantic.ai.golem.api.backend.GolemException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// TODO what will be the name of this logger?
private val errorLogger = KotlinLogging.logger {}

@OptIn(ExperimentalUuidApi::class)
fun StatusPagesConfig.configureStatusPages() {

    // TODO add proper support
//    status(HttpStatusCode.NotFound) { call, status ->
//        call.respond(
//            status = HttpStatusCode.NotFound,
//        )
//    }

    exception<GolemException> { call, cause ->
        // TODO should we log golem exceptions here?
        logger.error(cause) { "Request error" }
        val status = when (cause.error) {
            is GolemError.NoSuchCognition -> HttpStatusCode.NotFound
            is GolemError.BadRequest -> HttpStatusCode.BadRequest
            is GolemError.Unexpected -> HttpStatusCode.InternalServerError
        }
        call.respond(
            status = status,
            message = cause.error
        )
    }

    exception<Exception> { call, cause ->
        val errorReference = Uuid.random().toString()
        val message = "Unexpected error occurred, ref: $errorReference"
        errorLogger.error(cause) { message }
        call.respond(
            status = HttpStatusCode.InternalServerError,
            message = GolemError.Unexpected(message)
        )
    }

}
