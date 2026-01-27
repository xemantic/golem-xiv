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

import com.xemantic.ai.anthropic.Anthropic
import com.xemantic.ai.golem.api.GolemError
import com.xemantic.ai.golem.api.GolemOutput
import com.xemantic.ai.golem.api.Phenomenon
import com.xemantic.ai.golem.api.backend.GolemException
import com.xemantic.ai.golem.cognizer.anthropic.AnthropicToolUseCognizer
import com.xemantic.ai.golem.core.GolemXiv
import com.xemantic.ai.golem.core.cognition.DefaultCognitionRepository
import com.xemantic.ai.golem.core.script.GolemScriptDependencyProvider
import com.xemantic.ai.golem.logging.initializeLogging
import com.xemantic.ai.golem.neo4j.Neo4jCognitiveMemory
import com.xemantic.ai.golem.neo4j.Neo4jAgentIdentity
import com.xemantic.ai.golem.neo4j.Neo4jMemory
import com.xemantic.neo4j.driver.DispatchedNeo4jOperations
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopPreparing
//import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.config.property
import io.ktor.server.http.content.CompressedFileType
import io.ktor.server.http.content.staticResources
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
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase

val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    initializeLogging()
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    logger.info { "Starting Golem XIV server" }

    val neo4jConfig = property<Neo4jConfig>("neo4j")
    val authToken = if (neo4jConfig.username == "golem" && neo4jConfig.password == "golem") {
        AuthTokens.none()
    } else {
        AuthTokens.basic(neo4jConfig.username, neo4jConfig.password)
    }

    val outputs = MutableSharedFlow<GolemOutput>() // TODO can we move outputs to Golem?

    val driver = GraphDatabase.driver(neo4jConfig.uri, authToken)
    val httpClient = HttpClient()

    val neo4j = DispatchedNeo4jOperations(
        driver = driver,
        dispatcher = Dispatchers.IO.limitedParallelism(90)
    )
    val identity = Neo4jAgentIdentity(neo4j = neo4j)

    val repository = DefaultCognitionRepository(
        memory = Neo4jCognitiveMemory(
            neo4j = neo4j
        )
    )

    val anthropic = Anthropic {
        anthropicBeta = listOf(
            "fine-grained-tool-streaming-2025-05-14",
            "token-efficient-tools-2025-02-19",
            "prompt-caching-2024-07-31"
        )
    }

    val anthropicCognizer = AnthropicToolUseCognizer(
        anthropic = anthropic,
        golemSelfId = runBlocking { // TODO should be solved much better
            identity.getSelfId()
        },
        repository = repository
    )


    val golemScriptDependencyProvider = GolemScriptDependencyProvider(
        repository = repository,
        memoryProvider = { cognitionId, fulfillmentId ->
            Neo4jMemory(
                neo4j = neo4j,
                cognitionId = cognitionId,
                fulfillmentId = fulfillmentId
            )
        }
    )

    val golem = GolemXiv(
        identity = identity,
        repository = repository,
        cognizer = anthropicCognizer,
        golemScriptDependencyProvider = golemScriptDependencyProvider,
        outputs = outputs
    )

    monitor.subscribe(ApplicationStopPreparing) {
        logger.info { "Stopping Golem XIV server" }
        golem.close()
        driver.close()
        httpClient.close()
    }

    // TODO do we need to shutdown logback?
//    monitor.subscribe(ApplicationStopped) {
//        LogManager.shutdown()
//    }

    install(CallLogging) {
        level = org.slf4j.event.Level.DEBUG
    }
    install(ContentNegotiation) {
        json()
    }
    install(SSE)

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

        neo4jProxy(
            httpClient = httpClient,
            neo4jHttpUri = neo4jConfig.httpUri,
            username = neo4jConfig.username,
            password = neo4jConfig.password
        )

        sse("/events") {
            val clientIp = call.request.origin.remoteAddress
            logger.info { "SSE client connected: $clientIp" }

            sendGolemOutput(
                GolemOutput.Welcome("You are connected to Golem XIV")
            )

            outputs.collect {
                sendGolemOutput(it)
            }
        }
    }
}

fun Route.golemApiRoute(
    logger: KLogger,
    golem: GolemXiv
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
