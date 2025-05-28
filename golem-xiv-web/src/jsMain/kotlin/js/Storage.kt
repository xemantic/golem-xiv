/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.js

import org.w3c.dom.Storage

fun Storage.setItems(
    vararg items: Pair<String, String>
) {
    items.forEach { (key, value) ->
        setItem(key, value)
    }
}
