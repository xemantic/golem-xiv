/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import org.intellij.lang.annotations.Language
import org.neo4j.driver.Result
import org.neo4j.driver.TransactionContext
import org.neo4j.driver.Value
import kotlin.time.toKotlinInstant

fun Value.asInstant() = this.asZonedDateTime().toInstant().toKotlinInstant()

fun TransactionContext.runCypher(
    @Language("cypher") query: String?,
    parameters: Map<String, Any?> = emptyMap()
): Result = run(query, parameters)
