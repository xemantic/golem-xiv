/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.neo4j

import org.neo4j.driver.Value
import kotlin.time.toKotlinInstant

fun Value.asInstant() = this.asZonedDateTime().toInstant().toKotlinInstant()
