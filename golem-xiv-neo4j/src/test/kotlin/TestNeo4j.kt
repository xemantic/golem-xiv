/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import com.xemantic.ai.golem.logging.initializeLogging
import com.xemantic.neo4j.driver.Neo4jOperations
import org.intellij.lang.annotations.Language
import org.neo4j.configuration.connectors.BoltConnector
import org.neo4j.configuration.connectors.HttpConnector
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.harness.Neo4j
import org.neo4j.harness.internal.InProcessNeo4jBuilder

object TestNeo4j {

    init {
        initializeLogging()
    }

    private val db: Neo4j by lazy {
        InProcessNeo4jBuilder()
            .withDisabledServer()
            .withConfig(HttpConnector.enabled, false)
            .withConfig(BoltConnector.enabled, true)
            .build()
    }

    private val config: Neo4jConfig by lazy {
        Neo4jConfig(
            uri = db.boltURI().toString(),
            user = "",
            password = "",
            maxConcurrentSessions = 90
        )
    }

    private val driver: Driver by lazy {
        GraphDatabase.driver(
            config.uri,
            AuthTokens.none()
        ).apply {
            applyMigrations(
                driver = this
            )
        }
    }

    val operations: Neo4jOperations by lazy {
        neo4jOperations(
            driver = driver,
            config = config
        )
    }

    suspend fun populate(
        @Language("cypher") query: String
    ) {
        operations.populate(query)
    }

    fun cleanDatabase() {
        driver.executableQuery(
            "MATCH (n) DETACH DELETE n"
        ).execute()
    }

}
