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

package com.xemantic.ai.golem.neo4j

import ac.simons.neo4j.migrations.core.Migrations
import ac.simons.neo4j.migrations.core.MigrationsConfig
import com.xemantic.neo4j.driver.DispatchedNeo4jOperations
import com.xemantic.neo4j.driver.Neo4jOperations
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase

/**
 * Maps properties from `application.yaml`.
 */
@Serializable
data class Neo4jConfig(
    val uri: String,
    val user: String,
    val password: String,
    val maxConcurrentSessions: Int
)

fun neo4jDriver(
    config: Neo4jConfig
): Driver = GraphDatabase.driver(
    config.uri,
    AuthTokens.basic(config.user, config.password)
).apply {
    verifyConnectivity()
    // Apply migrations after connectivity is verified but before driver is used
    applyMigrations(driver = this)
}

fun neo4jOperations(
    driver: Driver,
    config: Neo4jConfig
): Neo4jOperations = DispatchedNeo4jOperations(
    driver = driver,
    dispatcher = Dispatchers.IO.limitedParallelism(
        parallelism = config.maxConcurrentSessions,
        name = "neo4j"
    )
)

/**
 * Applies Neo4j migrations.
 *
 * Migration location: `classpath:neo4j/migrations
 *
 * If any migration fails, an exception is thrown and the application will not start.
 *
 * @param driver The Neo4j driver with verified connectivity
 * @throws Exception if migrations fail
 */
fun applyMigrations(
    driver: Driver,
) {

    logger.info { "Applying migrations..." }

    val appMigrationsConfig = MigrationsConfig.builder()
        .withLocationsToScan("classpath:neo4j/migrations")
        .withTransactionMode(MigrationsConfig.TransactionMode.PER_STATEMENT)
        .build()

    val appMigrations = Migrations(appMigrationsConfig, driver)

    try {
        appMigrations.apply()
        logger.info { "Migrations applied" }
    } catch (e: Exception) {
        logger.error(e) { "Failed to apply application migrations" }
        throw e
    }

    logger.info { "All migrations applied successfully" }

}

private val logger = KotlinLogging.logger {}
