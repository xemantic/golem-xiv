/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.golem.api.GolemError
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.GolemException
import com.xemantic.ai.golem.api.backend.script.Files
import com.xemantic.ai.golem.api.backend.script.Memory
import com.xemantic.ai.golem.cognizer.anthropic.AnthropicToolUseCognizer
import com.xemantic.ai.golem.core.Golem
import com.xemantic.ai.golem.core.cognition.DefaultCognitionRepository
import com.xemantic.ai.golem.core.script.service.DefaultFiles
import com.xemantic.ai.golem.core.service
import com.xemantic.ai.golem.neo4j.Neo4jCognitiveMemory
import com.xemantic.ai.golem.neo4j.Neo4jAgentIdentity
import com.xemantic.ai.golem.neo4j.Neo4jMemory
import com.xemantic.ai.golem.storage.file.FileCognitionStorage
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
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
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
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
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import java.io.File

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

    val neo4j = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.none())

    val identity = Neo4jAgentIdentity(driver = neo4j)

    val repository = DefaultCognitionRepository(
        memory = Neo4jCognitiveMemory(
            driver = neo4j
        ),
        storage = FileCognitionStorage(File("var/cognitions"))
    )

    val anthropic = Anthropic()

    val anthropicCognizer = AnthropicToolUseCognizer(
        anthropic = anthropic,
        golemSelfId = runBlocking { // TODO should be solved much better
            identity.getSelfId()
        },
        repository = repository
    )

    val files = DefaultFiles()

    val golemScriptDependencies = listOf(
//            service<com.xemantic.ai.golem.server.script.Context>("phenomena", com.xemantic.ai.golem.server.script.service.DefaultContext(scope, outputs)),
        service<Files>("files", files),
        //service<WebBrowser>("browser", DefaultWebBrowser(browser)),
        service<Memory>("memory", Neo4jMemory(neo4j))
////            service<WebBrowserService>("webBrowserService", DefaultWebBrowserService())
////                    service<StringEditorService>("stringEditorService", stringEditorService())
    )

    val golem = Golem(
        identity = identity,
        repository = repository,
        cognizer = anthropicCognizer,
        golemScriptDependencies = golemScriptDependencies,
        outputs = outputs
    )

    monitor.subscribe(ApplicationStopPreparing) {
        logger.info { "Stopping Golem XIV server" }
        golem.close()
        neo4j.close()
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

    install(StatusPages) {
        configureStatusPages()
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
                    sendGolemOutput(it)
                }
            }

            // not used at the moment
            collectGolemInput {
                logger.debug {
                    "GolemInput received: $it"
                }
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

    get("/cognitions") {
//        call.respond(
//            golem.contexts
//        )
    }

    put("/cognitions") {

        logger.debug {
            "Cognition: initiating"
        }

        val phenomena = call.receive<List<Phenomenon>>()
        val cognitionId = golem.initiateCognition()
        call.respond(cognitionId)
        golem.perceive(
            cognitionId = cognitionId,
            phenomena = phenomena
        )
    }

    patch("/cognitions/{id}") {

        val id = parseCognitionId()

        logger.debug {
            "Cognition[$id]: continuing"
        }

        val phenomena = call.receive<List<Phenomenon>>()
        golem.perceive(
            cognitionId = id,
            phenomena = phenomena
        )
        call.respond("ok") // TODO what should be returned here?
    }

    get("/cognitions/{id}") {

        val id = parseCognitionId()

        logger.debug {
            "Cognition[$id]: emitting via WebSocket"
        }

        call.respond("ok")

        golem.emitCognition(id)
    }

    post("/cognitions") {

    }

}

private fun RoutingContext.parseCognitionId(): Long {
    val paramId = call.parameters["id"]
    if (paramId == null) {
        throw GolemException(
            GolemError.BadRequest(
                "The cognition id must be specified in uri: ${call.request.uri}"
            )
        )
    }
    return try {
        paramId.toLong()
    } catch (e: NumberFormatException) {
        throw GolemException(
            error = GolemError.BadRequest(
                "The cognition id must be specified in uri: ${call.request.uri}"
            ),
            cause = e
        )
    }
}
