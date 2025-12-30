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

package com.xemantic.ai.golem.neo4j.starter

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
