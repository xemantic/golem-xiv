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

package com.xemantic.ai.golem.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState
import com.xemantic.ai.golem.api.backend.script.WebBrowser
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

private const val DEFAULT_NAVIGATION_TIMEOUT_MS = 30_000.0

/**
 * Playwright-based WebBrowser implementation.
 *
 * Supports both stateless page fetches and persistent named sessions.
 *
 * @param browser The Playwright browser instance
 * @param navigationTimeoutMs Timeout for page navigation in milliseconds
 * @param keepPagesOpen When true, keeps the browser page open after stateless fetches.
 *                      Useful for debugging with visible browser (--show-browser mode).
 *                      When false (default), pages are closed immediately after use.
 */
class DefaultWebBrowser(
    private val browser: Browser,
    private val navigationTimeoutMs: Double = DEFAULT_NAVIGATION_TIMEOUT_MS,
    private val keepPagesOpen: Boolean = false
) : WebBrowser, Closeable {

    private class Session(
        val context: BrowserContext
    ) {
        val mutex = Mutex()
        var page: Page? = null
    }

    private val sessions = ConcurrentHashMap<String, Session>()
    private val sessionCreationMutex = Mutex()

    // For keepPagesOpen mode: reuse a single page for stateless calls
    private val statelessPageMutex = Mutex()
    private var statelessPage: Page? = null

    override suspend fun open(url: String): String {
        return if (keepPagesOpen) {
            openKeepingPageOpen(url)
        } else {
            openWithFreshPage(url)
        }
    }

    private suspend fun openWithFreshPage(url: String): String = withContext(Dispatchers.IO) {
        logger.debug { "Opening URL (stateless, fresh page): $url" }

        // Fresh page for each stateless request
        val page = browser.newPage()
        try {
            page.navigate(
                url,
                Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    .setTimeout(navigationTimeoutMs)
            )
            val html = page.content()

            logger.debug {
                buildString {
                    appendLine("Playwright HTML content for URL: $url")
                    appendLine("HTML length: ${html.length} characters")
                    appendLine("First 1000 characters:")
                    appendLine(html.take(1000))
                    if (html.length > 1000) {
                        appendLine("...")
                    }
                }
            }

            val markdown = convertHtmlToMarkdown(html)

            logger.debug {
                buildString {
                    appendLine("HTML-to-Markdown conversion complete for URL: $url")
                    appendLine("Markdown length: ${markdown.length} characters")
                    appendLine("First 500 characters:")
                    appendLine(markdown.take(500))
                    if (markdown.length > 500) {
                        appendLine("...")
                        appendLine("Last 200 characters:")
                        appendLine(markdown.takeLast(200))
                    }
                }
            }

            markdown
        } finally {
            page.close()
        }
    }

    private suspend fun openKeepingPageOpen(url: String): String = statelessPageMutex.withLock {
        withContext(Dispatchers.IO) {
            logger.debug { "Opening URL (stateless, keeping page open): $url" }

            // Reuse existing page or create new one
            val page = statelessPage ?: browser.newPage().also {
                statelessPage = it
                logger.debug { "Created new stateless page (will be kept open)" }
            }

            page.navigate(
                url,
                Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    .setTimeout(navigationTimeoutMs)
            )
            val html = page.content()

            logger.debug {
                buildString {
                    appendLine("Playwright HTML content for URL: $url")
                    appendLine("HTML length: ${html.length} characters")
                    appendLine("First 1000 characters:")
                    appendLine(html.take(1000))
                    if (html.length > 1000) {
                        appendLine("...")
                    }
                }
            }

            val markdown = convertHtmlToMarkdown(html)

            logger.debug {
                buildString {
                    appendLine("HTML-to-Markdown conversion complete for URL: $url")
                    appendLine("Markdown length: ${markdown.length} characters")
                    appendLine("First 500 characters:")
                    appendLine(markdown.take(500))
                    if (markdown.length > 500) {
                        appendLine("...")
                        appendLine("Last 200 characters:")
                        appendLine(markdown.takeLast(200))
                    }
                }
            }

            // Page stays open - visible in browser window
            markdown
        }
    }

    override suspend fun openInSession(sessionId: String, url: String): String {
        logger.debug { "Opening URL in session '$sessionId': $url" }

        val session = getOrCreateSession(sessionId)

        return session.mutex.withLock {
            withContext(Dispatchers.IO) {
                // Reuse page within session to preserve navigation state
                val page = session.page ?: session.context.newPage().also {
                    session.page = it
                }

                page.navigate(
                    url,
                    Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.NETWORKIDLE)
                        .setTimeout(navigationTimeoutMs)
                )

                val html = page.content()

                logger.debug {
                    buildString {
                        appendLine("Session '$sessionId' HTML content for URL: $url")
                        appendLine("HTML length: ${html.length} characters")
                        appendLine("First 1000 characters:")
                        appendLine(html.take(1000))
                        if (html.length > 1000) {
                            appendLine("...")
                        }
                    }
                }

                val markdown = convertHtmlToMarkdown(html)

                logger.debug {
                    buildString {
                        appendLine("Session '$sessionId' conversion complete for URL: $url")
                        appendLine("Markdown length: ${markdown.length} characters")
                    }
                }

                markdown
            }
        }
    }

    override suspend fun closeSession(sessionId: String) {
        logger.debug { "Closing session: $sessionId" }

        sessions.remove(sessionId)?.let { session ->
            withContext(Dispatchers.IO) {
                session.page?.close()
                session.context.close()
            }
        }
    }

    override fun listSessions(): Set<String> = sessions.keys.toSet()

    private suspend fun getOrCreateSession(sessionId: String): Session {
        // Fast path
        sessions[sessionId]?.let { return it }

        // Slow path with synchronization
        return sessionCreationMutex.withLock {
            sessions.getOrPut(sessionId) {
                logger.debug { "Creating new session: $sessionId" }
                val context = withContext(Dispatchers.IO) {
                    browser.newContext()
                }
                Session(context)
            }
        }
    }

    override fun close() {
        logger.debug { "Closing DefaultWebBrowser with ${sessions.size} active sessions" }
        // Close stateless page if kept open
        statelessPage?.close()
        statelessPage = null
        // Close all session pages and contexts
        sessions.values.forEach { session ->
            session.page?.close()
            session.context.close()
        }
        sessions.clear()
    }

}

/**
 * Converts HTML to Markdown using jsoup for parsing and custom conversion logic.
 * This gives us more control over the output than Flexmark and can be extended
 * to use markanywhere's semantic events in the future.
 */
private fun convertHtmlToMarkdown(html: String): String {
    val doc = Jsoup.parse(html)

    // Remove script and style elements
    doc.select("script, style, noscript").remove()

    val converter = HtmlToMarkdownConverter()
    return converter.convert(doc.body())
}

private class HtmlToMarkdownConverter {
    private val output = StringBuilder()
    private var listDepth = 0
    private var needsLeadingSpace = false

    fun convert(element: Element): String {
        processNode(element)
        return output.toString().trim()
    }

    private fun processNode(node: Node) {
        when (node) {
            is TextNode -> {
                val wholeText = node.wholeText
                if (wholeText.isNotBlank()) {
                    // Normalize whitespace but preserve word boundaries
                    val normalized = wholeText.replace(Regex("\\s+"), " ")
                    // Add leading space if needed (after previous inline element)
                    if (needsLeadingSpace && normalized.startsWith(" ").not() && output.isNotEmpty() && output.last() !in listOf('\n', ' ', '[', '(')) {
                        output.append(" ")
                    }
                    output.append(normalized.trim())
                    // Track if we need space before next element
                    needsLeadingSpace = normalized.endsWith(" ").not()
                } else if (wholeText.contains(" ") || wholeText.contains("\n")) {
                    // Preserve single space between inline elements
                    needsLeadingSpace = true
                }
            }
            is Element -> processElement(node)
        }
    }

    private fun processElement(element: Element) {
        when (element.tagName().lowercase()) {
            "h1" -> processHeading(element, 1)
            "h2" -> processHeading(element, 2)
            "h3" -> processHeading(element, 3)
            "h4" -> processHeading(element, 4)
            "h5" -> processHeading(element, 5)
            "h6" -> processHeading(element, 6)
            "p" -> processParagraph(element)
            "br" -> output.append("\n")
            "a" -> processLink(element)
            "img" -> processImage(element)
            "strong", "b" -> processStrong(element)
            "em", "i" -> processEmphasis(element)
            "code" -> processInlineCode(element)
            "pre" -> processCodeBlock(element)
            "ul" -> processList(element, false)
            "ol" -> processList(element, true)
            "li" -> processListItem(element)
            "blockquote" -> processBlockquote(element)
            "hr" -> output.append("\n\n---\n\n")
            "table" -> processTable(element)
            "div", "section", "article", "main", "header", "footer", "nav" -> {
                // Process children for structural elements
                element.childNodes().forEach { processNode(it) }
            }
            else -> {
                // For unknown elements, just process their children
                element.childNodes().forEach { processNode(it) }
            }
        }
    }

    private fun processHeading(element: Element, level: Int) {
        output.append("\n\n")
        output.append("#".repeat(level))
        output.append(" ")
        needsLeadingSpace = false
        element.childNodes().forEach { processNode(it) }
        output.append("\n\n")
        needsLeadingSpace = false
    }

    private fun processParagraph(element: Element) {
        output.append("\n\n")
        needsLeadingSpace = false
        element.childNodes().forEach { processNode(it) }
        output.append("\n\n")
        needsLeadingSpace = false
    }

    private fun processLink(element: Element) {
        if (needsLeadingSpace && output.isNotEmpty() && output.last() !in listOf('\n', ' ')) {
            output.append(" ")
        }
        val href = element.attr("href")
        output.append("[")
        needsLeadingSpace = false
        element.childNodes().forEach { processNode(it) }
        output.append("](")
        output.append(href)
        output.append(")")
        needsLeadingSpace = false
    }

    private fun processImage(element: Element) {
        val src = element.attr("src")
        val alt = element.attr("alt")
        output.append("![")
        output.append(alt)
        output.append("](")
        output.append(src)
        output.append(")")
    }

    private fun processStrong(element: Element) {
        if (needsLeadingSpace && output.isNotEmpty() && output.last() !in listOf('\n', ' ')) {
            output.append(" ")
        }
        output.append("**")
        needsLeadingSpace = false
        element.childNodes().forEach { processNode(it) }
        output.append("**")
        needsLeadingSpace = false
    }

    private fun processEmphasis(element: Element) {
        if (needsLeadingSpace && output.isNotEmpty() && output.last() !in listOf('\n', ' ')) {
            output.append(" ")
        }
        output.append("*")
        needsLeadingSpace = false
        element.childNodes().forEach { processNode(it) }
        output.append("*")
        needsLeadingSpace = false
    }

    private fun processInlineCode(element: Element) {
        if (needsLeadingSpace && output.isNotEmpty() && output.last() !in listOf('\n', ' ')) {
            output.append(" ")
        }
        output.append("`")
        output.append(element.text())
        output.append("`")
        needsLeadingSpace = false
    }

    private fun processCodeBlock(element: Element) {
        output.append("\n\n```\n")
        output.append(element.text())
        output.append("\n```\n\n")
        needsLeadingSpace = false
    }

    private fun processList(element: Element, ordered: Boolean) {
        listDepth++
        var itemIndex = 1
        element.children().forEach { child ->
            if (child.tagName() == "li") {
                processListItem(child, if (ordered) itemIndex++ else null)
            }
        }
        listDepth--
        if (listDepth == 0) {
            output.append("\n")
        }
        needsLeadingSpace = false
    }

    private fun processListItem(element: Element, orderedIndex: Int? = null) {
        output.append("\n")
        output.append("  ".repeat(listDepth - 1))
        output.append(if (orderedIndex != null) "$orderedIndex. " else "- ")
        needsLeadingSpace = false
        element.childNodes().forEach { processNode(it) }
    }

    private fun processBlockquote(element: Element) {
        output.append("\n\n> ")
        needsLeadingSpace = false
        element.childNodes().forEach { processNode(it) }
        output.append("\n\n")
        needsLeadingSpace = false
    }

    private fun processTable(element: Element) {
        output.append("\n\n")

        // Determine the maximum number of columns (considering colspan)
        val rows = element.select("tr")
        if (rows.isEmpty()) return

        val firstRow = rows.first()!!

        // Calculate column count from first row (accounting for colspan)
        val columnCount = firstRow.select("th, td").sumOf { cell ->
            cell.attr("colspan").toIntOrNull() ?: 1
        }

        rows.forEachIndexed { rowIndex, row ->
            val cells = row.select("th, td")
            output.append("|")

            var colIndex = 0
            cells.forEach { cell ->
                val colspan = cell.attr("colspan").toIntOrNull() ?: 1
                val cellText = cell.text()

                // Output the cell content
                output.append(" $cellText |")

                // Add empty cells for colspan > 1
                repeat(colspan - 1) {
                    output.append(" |")
                }
                colIndex += colspan
            }

            // Fill remaining columns if row is short
            while (colIndex < columnCount) {
                output.append(" |")
                colIndex++
            }

            output.append("\n")

            // Add header separator after first row
            if (rowIndex == 0) {
                output.append("|")
                repeat(columnCount) {
                    output.append(" --- |")
                }
                output.append("\n")
            }
        }
        output.append("\n")
        needsLeadingSpace = false
    }
}
