/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import org.neo4j.configuration.GraphDatabaseSettings
import org.neo4j.configuration.connectors.BoltConnector
import org.neo4j.configuration.connectors.HttpConnector
import org.neo4j.configuration.helpers.SocketAddress
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
import java.nio.file.Path
import kotlin.io.path.absolute

fun main() {
    val dbDir = Path.of("../var/neo4j").absolute().normalize()
    val managementService = DatabaseManagementServiceBuilder(dbDir)
        .setConfig(GraphDatabaseSettings.auth_enabled, false)
        // 128MB - small development datasets
        .setConfig(GraphDatabaseSettings.pagecache_memory, 134217728L)
        .setConfig(BoltConnector.enabled, true)
        .setConfig(BoltConnector.listen_address, SocketAddress("localhost", 7687))
        .setConfig(HttpConnector.enabled, true)
        .setConfig(HttpConnector.listen_address, SocketAddress("localhost", 7474))
        .build()
    Runtime.getRuntime().addShutdownHook(Thread {
        managementService.shutdown()
    })
}
