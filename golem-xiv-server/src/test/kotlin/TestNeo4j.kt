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

import org.neo4j.configuration.connectors.BoltConnector
import org.neo4j.configuration.connectors.HttpConnector
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.harness.Neo4j
import org.neo4j.harness.internal.InProcessNeo4jBuilder

/**
 * Test utility that provides an embedded Neo4j instance using neo4j-harness.
 * Used for integration tests that require a running Neo4j database.
 */
object TestNeo4j {

    private val db: Neo4j by lazy {
        InProcessNeo4jBuilder()
            .withDisabledServer()
            .withConfig(HttpConnector.enabled, false)
            .withConfig(BoltConnector.enabled, true)
            .build()
    }

    private val driver: Driver by lazy {
        GraphDatabase.driver(
            db.boltURI(),
            AuthTokens.none()
        )
    }

    /**
     * Ensures the Neo4j instance is initialized.
     * Call this property to start the embedded database before running tests.
     */
    val isInitialized: Boolean
        get() {
            // Access driver to trigger lazy initialization
            driver
            return true
        }

    fun cleanDatabase() {
        driver.executableQuery(
            "MATCH (n) DETACH DELETE n"
        ).execute()
    }

}
