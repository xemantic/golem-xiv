/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server

import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.server.server.collectGolemInput
import com.xemantic.ai.golem.server.server.sendGolemOutput
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopPreparing
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.CompressedFileType
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.origin
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager

val logger = KotlinLogging.logger {}

fun main() {
    val server = embeddedServer(Netty, port = 8081) {
        module()
    }
    server.start(wait = true)
}

fun Application.module() {

    logger.info { "Starting Golem XIV server" }

    val outputs = MutableSharedFlow<GolemOutput>() // TODO can we move outputs to Golem?
    val golem = Golem(outputs)

    monitor.subscribe(ApplicationStopPreparing) {
        logger.info { "Stopping Golem XIV server" }
        golem.close()
    }

    monitor.subscribe(ApplicationStopped) {
        LogManager.shutdown()
    }

    install(CallLogging) {
        level = org.slf4j.event.Level.DEBUG
    }
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        // Allow requests from any host
        anyHost()

        // Or specify allowed hosts
        // allowHost("example.com")
        // allowHost("localhost:3000")

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        // Allow headers
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        // Allow credentials (cookies, etc.)
        allowCredentials = true

        // Allow specific content types
        allowHeadersPrefixed("X-Custom-")

        // Configure max age for preflight requests cache
        maxAgeInSeconds = 3600
    }
    install(WebSockets) {
//        contentConverter = KotlinxWebsocketSerializationConverter(Json)
//        pingPeriod = 15.seconds
//        timeout = 15.seconds
//        maxFrameSize = Long.MAX_VALUE
//        masking = false
    }
    routing {

        staticResources("/", "web") {
            // does it matter for the local server?
            preCompressed(
                CompressedFileType.BROTLI,
                CompressedFileType.GZIP
            )
        }

        route("/api") {
            golemApiRoute(logger, golem)
        }

        webSocket("/ws") {

            val clientIp = call.request.origin.remoteAddress
            logger.info { "WebSocket connected: $clientIp" }

            sendGolemOutput(
                GolemOutput.Welcome("You are connected to Golem XIV")
            )

//            val frame = incoming.receive()
//            logger.info { frame }
//            val input = receiveGolemInput()
//            println(input)
//

            launch {
                outputs.collect {
                    logger.debug { "Go: $it" }
                    sendGolemOutput(it)
                }
            }

            collectGolemInput {
                logger.debug { it }
            }

//                try {
//                    val context = golem.newContext()
//
////                while (true) {
////                    val frame = incoming.receive()
////                    if (frame !is Frame.Text) {              logger.error { "Received frame is not a text: ${frame.frameType}" }
////                        send(AgentOutput.Error("Received frame is not a text: ${frame.frameType}"))              continue
////                    }            val receivedText = frame.readText()
////                    val message = try {
////                        AgentInput.fromJson(receivedText)            } catch (e: SerializationException) {
////                        logger.error { "Unrecognized JSON message: $receivedText" }              send(AgentOutput.Error("Unrecognized JSON message: ${frame.frameType}"))
////                        continue            }
////                    claudineSession.process(message) {              send(it)
////                    }
////                }        } catch (e: Exception) {
//
//                } catch (e: Exception) {
//                    logger.error {
//                        "Unexpected error in WebSocket session: ${e.message}"
//                    }
//                }
        }
    }
}

fun Route.golemApiRoute(
    logger: KLogger,
    golem: Golem
) {

    get("/ping") {
        call.respondText("pong")
    }

    get("/workspaces") {
//        call.respond(
//            golem.contexts
//        )
    }

    put("/workspaces") {
        val phenomena = call.receive<List<Phenomenon>>()
        val workspace = golem.newCognitiveWorkspace()
        val expression = workspace.structure(phenomena)
        call.respond(workspace.id)
        workspace.integrate(expression)
    }

    patch("/workspaces/{id}") {
        logger.debug { "Updating context: start" }
        val id = requireNotNull(call.parameters["id"]) { "Should never happen" }
        val phenomena = call.receive<List<Phenomenon>>()
        val workspace = golem.getCognitiveWorkspace(id)
        if (workspace == null) {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = "Cognitive workspace not found: $id"
            )
        } else {
            val expression = workspace.structure(phenomena)
            call.respond("Phenomena received")
            workspace.integrate(expression)
        }
    }

    get("/workspaces/{id}") {
//        call.respond(
//            golem.contexts
//        )
    }

    post("/workspaces") {

    }

}
