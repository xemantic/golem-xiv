/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.client.http

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
            "${status.value} (${status.description})"
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
            "${status.value} (${status.description})"
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
                "${status.value} (${status.description})"
            )
        }
    }
}
