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

package com.xemantic.ai.golem.api.client.http

import com.xemantic.ai.golem.api.GolemError
import com.xemantic.ai.golem.api.client.GolemServiceException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

suspend inline fun <reified T> HttpClient.serviceGet(
    uri: String
): T = get(uri).run {
    if (status.isSuccess()) {
        body<T>()
    } else {
        throw GolemServiceException(
            uri,
            error = body<GolemError>()
        )
    }
}

suspend inline fun <reified I, reified O> HttpClient.servicePut(
    uri: String,
    value: I
): O = put(uri) {
    setBody<I>(value)
    contentType(ContentType.Application.Json)
}.run {
    if (status.isSuccess()) {
        body<O>()
    } else {
        throw GolemServiceException(
            uri,
            error = body<GolemError>()
        )
    }
}

suspend inline fun <reified I> HttpClient.servicePatch(
    uri: String,
    value: I
) {
    patch(uri) {
        setBody<I>(value)
        contentType(ContentType.Application.Json)
    }.run {
        if (!status.isSuccess()) {
            throw GolemServiceException(
                uri,
                error = body<GolemError>()
            )
        }
    }
}
