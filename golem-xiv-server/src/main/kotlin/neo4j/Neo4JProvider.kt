/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.neo4j

import org.neo4j.configuration.GraphDatabaseSettings
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
import java.nio.file.Path

class Neo4JProvider {

    private val databaseDirectory = Path.of("neo4j")

    // Create and start the database management service
    private val managementService = DatabaseManagementServiceBuilder(
        databaseDirectory
    ).build()

    // Get the default database
    val graphDb = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME)

    fun close() {
        managementService.shutdown()
    }

}