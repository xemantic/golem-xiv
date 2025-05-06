/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.js

import kotlinx.html.HTMLTag

var HTMLTag.ariaLabel: String
    get() = attributes["aria-label"]!!
    set(value) {
        attributes["aria-label"] = value
    }
