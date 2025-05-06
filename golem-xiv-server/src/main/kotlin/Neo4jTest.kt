/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server

import com.xemantic.ai.golem.server.neo4j.Neo4JProvider

fun main() {
    val dbManger = Neo4JProvider()
    // Example of a transaction with the database
    dbManger.graphDb.executeTransactionally("CREATE (n:Person {name: 'Alice'})")

// Query the database and process results
    val result = dbManger.graphDb.executeTransactionally(
        "MATCH (n:Person) RETURN n.name AS name",
        emptyMap()
    ) { result ->
        result.resultAsString()
    }
    println(result)
    dbManger.close()
}
