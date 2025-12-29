package com.xemantic.ai.golem.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

// cors is needed only during development
fun Application.cors() {
    install(CORS) {
        // Allow requests from any host
        anyHost()

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        // Allow headers
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.CacheControl)

        // Allow specific content types
        allowHeadersPrefixed("X-Custom-")

        // Allow non-simple content types (needed for SSE text/event-stream)
        allowNonSimpleContentTypes = true

        // Expose headers needed by SSE
        exposeHeader(HttpHeaders.ContentType)

        // Configure max age for preflight requests cache
        maxAgeInSeconds = 3600
    }
}
