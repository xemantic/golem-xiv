/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server

import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.Prompt
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
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import kotlin.uuid.Uuid

val logger = KotlinLogging.logger {}

fun main() {
    val server = embeddedServer(Netty, port = 8081) {
        module()
    }
    server.start(wait = true)
}

fun Application.module() {

    logger.info { "Starting Golem XIV server" }

    val outputs = MutableSharedFlow<GolemOutput>()
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
            golemApiRoute(logger, golem, outputs)
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
    golem: Golem,
    outputs: FlowCollector<GolemOutput>
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
        val context = golem.newContext()
        val message = context.createMessage(prompt)
        call.respond(context.info)
        logger.debug { "Context[${context.id}]: emitting initial message output" }
        outputs.emit(contextId = context.id, message)
        context.send(message)
    }

    patch("/contexts/{id}") {
        logger.debug { "Updating context: start" }
        val prompt = call.receive<Prompt>()
        val idParameter = requireNotNull(call.parameters["id"]) { "Should never happen" }
        val id = Uuid.parse(idParameter)
        val context = golem.getContext(id)
        if (context == null) {
            call.respond(HttpStatusCode.NotFound, "resource not found $")
        } else {
            val message = context.createMessage(prompt)
            call.respond(context.info)
            outputs.emit(contextId = context.id, message)
            context.send(message)
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
