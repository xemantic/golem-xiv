/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.api.backend

import com.xemantic.ai.golem.api.GolemError

class GolemException(
    val error: GolemError,
    cause: Throwable? = null
) : RuntimeException(
    error.toString(),
    cause
)
