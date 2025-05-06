/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.web.view

import kotlinx.browser.document
import kotlinx.html.dom.create
import org.w3c.dom.HTMLElement

interface HtmlView {

    val element: HTMLElement

    val html get() = document.create

}
