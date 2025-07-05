/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.backend.script.incubation

interface WebBrowser {
    /** @return given [url] as Markdown. */
    suspend fun open(url: String): String
}

