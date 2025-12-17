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
