/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.server.script.service

import com.xemantic.ai.golem.server.script.Memory
import com.xemantic.ai.golem.server.script.Node
import io.github.oshai.kotlinlogging.KotlinLogging

class DefaultMemory : Memory {

    private val logger = KotlinLogging.logger {}

    override fun storeFact(
        sourceNode: Node,
        relationship: String,
        targetNode: Node,
        properties: Map<String, Any>
    ): Long {
        logger.debug { "Storing fact: $sourceNode:$relationship:$targetNode [$properties]" }
        return 0
    }

    override fun createNode(
        labels: List<String>,
        properties: Map<String, Any>
    ): Node {
        logger.debug { "Creating node: $labels: [$properties]" }
        return Node(0, labels, properties)
    }

}
