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

//import io.ktor.server.application.ApplicationStopped
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
import com.xemantic.ai.golem.neo4j.Neo4jAgentIdentity
import com.xemantic.ai.golem.neo4j.Neo4jCognitiveMemory
import com.xemantic.ai.golem.neo4j.Neo4jMemory
import com.xemantic.neo4j.driver.DispatchedNeo4jOperations
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase

val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    initializeLogging()

    // Parse custom CLI arguments
    val showBrowser = args.contains("--show-browser")
    System.setProperty("golem.playwright.headless", (!showBrowser).toString())

    // Parse --chromium-path argument
    val chromiumPathArg = args.find { it.startsWith("--chromium-path=") }
    chromiumPathArg?.let {
        val path = it.substringAfter("--chromium-path=")
        System.setProperty("golem.playwright.chromium.path", path)
        logger.info { "Custom chromium path specified: $path" }
    }

    // Filter out custom arguments before passing to Ktor
    val ktorArgs = args.filter {
        it != "--show-browser" && !it.startsWith("--chromium-path=")
    }.toTypedArray()

    logger.info { "Playwright headless mode: ${!showBrowser}" }

    io.ktor.server.netty.EngineMain.main(ktorArgs)
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

    // Try to create Playwright browser for web content fetching (optional)
    var playwright: Playwright? = null
    var browser: com.microsoft.playwright.Browser? = null
    var webBrowser: com.xemantic.ai.golem.api.backend.script.WebBrowser? = null

    val headless = System.getProperty("golem.playwright.headless", "true").toBoolean()
    val customChromiumPath = System.getProperty("golem.playwright.chromium.path")

    try {
        playwright = Playwright.create()

        // If custom chromium path is specified, use it directly
        if (customChromiumPath != null) {
            logger.info { "Initializing Playwright with custom chromium at: $customChromiumPath (headless: $headless)" }
            browser = playwright.chromium().launch(
                BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setExecutablePath(Paths.get(customChromiumPath))
            )
            logger.info { "Playwright browser initialized successfully using custom chromium" }
        } else {
            // Try bundled Playwright chromium first
            try {
                logger.info { "Attempting to initialize Playwright with bundled chromium (headless: $headless)..." }
                browser = playwright.chromium().launch(
                    BrowserType.LaunchOptions()
                        .setHeadless(headless)
                )
                logger.info { "Playwright browser initialized successfully using bundled chromium" }
            } catch (bundledError: Exception) {
                logger.warn { "Failed to launch bundled chromium: ${bundledError.message}" }
                logger.info { "Falling back to system chromium..." }

                // Fall back to system chromium
                val systemChromiumPaths = listOf(
                    "/usr/bin/chromium",
                    "/usr/bin/chromium-browser",
                    "/usr/bin/google-chrome-stable",
                    // Newer ubuntu versions install chromium from snap,
                    // even if `sudo apt-get install chromium-browser`
                    // command is used
                    "/snap/bin/chromium"
                )

                var launched = false
                for (chromiumPath in systemChromiumPaths) {
                    if (Paths.get(chromiumPath).toFile().exists()) {
                        try {
                            logger.info { "Trying system chromium at: $chromiumPath" }
                            browser = playwright.chromium().launch(
                                BrowserType.LaunchOptions()
                                    .setHeadless(headless)
                                    .setExecutablePath(Paths.get(chromiumPath))
                            )
                            logger.info { "Playwright browser initialized successfully using system chromium at: $chromiumPath" }
                            launched = true
                            break
                        } catch (pathError: Exception) {
                            logger.warn { "Failed to launch chromium at $chromiumPath: ${pathError.message}" }
                        }
                    }
                }

                if (!launched) {
                    throw RuntimeException(
                        "Failed to launch both bundled and system chromium. " +
                        "Install chromium or specify custom path with --chromium-path"
                    )
                }
            }
        }

        webBrowser = DefaultWebBrowser(
            browser = browser!!,
            keepPagesOpen = !headless  // Keep page visible in --show-browser mode
        )
    } catch (e: Exception) {
        logger.warn(e) { "Failed to initialize Playwright browser, will use jina.ai fallback: ${e.message}" }
        browser?.close()
        playwright?.close()
        playwright = null
        browser = null
    }

    // Create HTTP client for Web service
    val webHttpClient = HttpClient(Java) {
        install(ClientContentNegotiation) {
            clientJson(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
            })
        }
    }

    // Create Web service with Playwright browser
    val web = DefaultWeb(
        httpClient = webHttpClient,
        webBrowser = webBrowser,
        ddgsServiceUrl = "http://localhost:8001"
    )

    val golemScriptDependencyProvider = GolemScriptDependencyProvider(
        repository = repository,
        memoryProvider = { cognitionId, fulfillmentId ->
            Neo4jMemory(
                neo4j = neo4j,
                cognitionId = cognitionId,
                fulfillmentId = fulfillmentId
            )
        },
        web = web
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
        browser?.close()
        playwright?.close()
        webHttpClient.close()
        driver.close()
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

        sse("/events") {
            val clientIp = call.request.origin.remoteAddress
            logger.info { "SSE client connected: $clientIp" }

            heartbeat()

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
