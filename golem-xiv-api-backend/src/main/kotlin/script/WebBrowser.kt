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

package com.xemantic.ai.golem.api.backend.script

/**
 * Browser automation for fetching web content as Markdown.
 *
 * Supports both stateless fetches and persistent sessions for
 * authenticated browsing.
 */
interface WebBrowser {

    /**
     * Opens a URL in a fresh, isolated page.
     *
     * No state is preserved between calls - each invocation gets
     * a clean browser context. Use this for simple content fetching
     * where authentication is not needed.
     *
     * @param url The URL to open
     * @return Page content as Markdown
     */
    suspend fun open(url: String): String

    /**
     * Opens a URL within a named session.
     *
     * Sessions persist cookies, localStorage, and authentication state
     * across calls. Use this when you need to:
     * - Log into a website and browse authenticated
     * - Maintain shopping cart state
     * - Preserve any stateful interaction
     *
     * Sessions are created lazily on first use and persist until
     * explicitly closed with [closeSession].
     *
     * @param sessionId Identifier for the session (e.g., "github", "my-bank")
     * @param url The URL to open
     * @return Page content as Markdown
     */
    suspend fun openInSession(sessionId: String, url: String): String

    /**
     * Closes a session and clears all its state.
     *
     * After calling this, the next [openInSession] with the same
     * sessionId will start fresh (logged out, no cookies).
     *
     * @param sessionId The session to close
     */
    suspend fun closeSession(sessionId: String)

    /**
     * Lists all active session IDs.
     */
    fun listSessions(): Set<String>

}
