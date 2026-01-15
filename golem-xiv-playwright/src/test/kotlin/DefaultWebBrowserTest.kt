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
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.xemantic.kotlin.test.assert
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains

class DefaultWebBrowserTest {

    companion object {
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser
        private lateinit var tempDir: Path

        @JvmStatic
        @BeforeAll
        fun setup() {
            playwright = Playwright.create()
            browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(true)
            )
            tempDir = Files.createTempDirectory("golem-web-browser-test")
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            browser.close()
            playwright.close()
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun `should convert simple HTML to Markdown`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <head><title>Test Page</title></head>
            <body>
                <h1>Main Heading</h1>
                <p>This is a simple paragraph with <strong>bold text</strong> and <em>italic text</em>.</p>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("simple.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "# Main Heading")
        assertContains(markdown, "This is a simple paragraph")
        assertContains(markdown, "**bold text**")
        assertContains(markdown, "*italic text*")
    }

    @Test
    fun `should convert links to Markdown format`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <p>Check out <a href="https://kotlinlang.org">Kotlin</a> for more info.</p>
                <a href="https://github.com">GitHub</a>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("links.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "[Kotlin](https://kotlinlang.org)")
        assertContains(markdown, "[GitHub](https://github.com)")
    }

    @Test
    fun `should convert lists to Markdown format`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <h2>Unordered List</h2>
                <ul>
                    <li>First item</li>
                    <li>Second item</li>
                    <li>Third item</li>
                </ul>
                <h2>Ordered List</h2>
                <ol>
                    <li>Step one</li>
                    <li>Step two</li>
                </ol>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("lists.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "## Unordered List")
        assertContains(markdown, "- First item")
        assertContains(markdown, "- Second item")
        assertContains(markdown, "- Third item")
        assertContains(markdown, "## Ordered List")
        assertContains(markdown, "1. Step one")
        assertContains(markdown, "2. Step two")
    }

    @Test
    fun `should convert code blocks to Markdown format`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <p>Inline code: <code>println("Hello")</code></p>
                <pre>fun main() {
    println("World")
}</pre>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("code.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "`println(\"Hello\")`")
        assertContains(markdown, "```")
        assertContains(markdown, "fun main()")
    }

    @Test
    fun `should convert images to Markdown format`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <img src="https://example.com/logo.png" alt="Company Logo">
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("image.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "![Company Logo](https://example.com/logo.png)")
    }

    @Test
    fun `should convert blockquotes to Markdown format`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <blockquote>
                    This is a quote from someone important.
                </blockquote>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("quote.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "> This is a quote")
    }

    @Test
    fun `should convert tables to Markdown format`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <table>
                    <tr>
                        <th>Name</th>
                        <th>Age</th>
                    </tr>
                    <tr>
                        <td>Alice</td>
                        <td>30</td>
                    </tr>
                    <tr>
                        <td>Bob</td>
                        <td>25</td>
                    </tr>
                </table>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("table.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "| Name | Age |")
        assertContains(markdown, "| --- | --- |")
        assertContains(markdown, "| Alice | 30 |")
        assertContains(markdown, "| Bob | 25 |")
    }

    @Test
    fun `should handle table with colspan`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <table>
                    <tr>
                        <th colspan="2">Full Name</th>
                        <th>Age</th>
                    </tr>
                    <tr>
                        <td>John</td>
                        <td>Doe</td>
                        <td>30</td>
                    </tr>
                </table>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("table-colspan.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        // Header should span 2 columns (Full Name + empty cell) + Age = 3 columns total
        assertContains(markdown, "| Full Name |")
        assertContains(markdown, "| --- | --- | --- |")
        assertContains(markdown, "| John | Doe | 30 |")
    }

    @Test
    fun `should handle table with missing cells`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <table>
                    <tr>
                        <th>A</th>
                        <th>B</th>
                        <th>C</th>
                    </tr>
                    <tr>
                        <td>1</td>
                        <td>2</td>
                    </tr>
                </table>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("table-missing.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "| A | B | C |")
        assertContains(markdown, "| --- | --- | --- |")
        // Row with missing cell should still have 3 columns
        assertContains(markdown, "| 1 | 2 |")
    }

    @Test
    fun `should handle table without headers`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <table>
                    <tr>
                        <td>A</td>
                        <td>B</td>
                    </tr>
                    <tr>
                        <td>1</td>
                        <td>2</td>
                    </tr>
                </table>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("table-no-headers.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        // Should still produce valid markdown table with separator after first row
        assertContains(markdown, "| A | B |")
        assertContains(markdown, "| --- | --- |")
        assertContains(markdown, "| 1 | 2 |")
    }

    @Test
    fun `should remove script and style tags`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { color: red; }
                </style>
            </head>
            <body>
                <p>Visible content</p>
                <script>
                    console.log("This should not appear");
                </script>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("scripts.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "Visible content")
        assert(!markdown.contains("color: red"))
        assert(!markdown.contains("console.log"))
    }

    @Test
    fun `should handle nested lists correctly`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <ul>
                    <li>Parent item 1
                        <ul>
                            <li>Child item 1.1</li>
                            <li>Child item 1.2</li>
                        </ul>
                    </li>
                    <li>Parent item 2</li>
                </ul>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("nested.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "- Parent item 1")
        assertContains(markdown, "- Parent item 2")
        assertContains(markdown, "Child item 1.1")
        assertContains(markdown, "Child item 1.2")
    }

    @Test
    fun `should convert complex document structure`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <head><title>Complex Page</title></head>
            <body>
                <header>
                    <h1>Site Header</h1>
                </header>
                <main>
                    <article>
                        <h2>Article Title</h2>
                        <p>Article content with <a href="/link">internal link</a>.</p>
                    </article>
                    <section>
                        <h3>Section Heading</h3>
                        <ul>
                            <li>Point 1</li>
                            <li>Point 2</li>
                        </ul>
                    </section>
                </main>
                <footer>
                    <p>Footer content</p>
                </footer>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("complex.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "# Site Header")
        assertContains(markdown, "## Article Title")
        assertContains(markdown, "### Section Heading")
        assertContains(markdown, "[internal link](/link)")
        assertContains(markdown, "- Point 1")
        assertContains(markdown, "Footer content")
    }

    @Test
    fun `should handle empty body`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <head><title>Empty Page</title></head>
            <body>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("empty.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assert(markdown.trim().isEmpty() || markdown.isBlank())
    }

    @Test
    fun `should convert multiple headings of different levels`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Level 1</h1>
                <h2>Level 2</h2>
                <h3>Level 3</h3>
                <h4>Level 4</h4>
                <h5>Level 5</h5>
                <h6>Level 6</h6>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("headings.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "# Level 1")
        assertContains(markdown, "## Level 2")
        assertContains(markdown, "### Level 3")
        assertContains(markdown, "#### Level 4")
        assertContains(markdown, "##### Level 5")
        assertContains(markdown, "###### Level 6")
    }

    @Test
    fun `should capture JavaScript-rendered content`() = runTest {
        // given - page where content is added by JavaScript after load
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <div id="container">Loading...</div>
                <script>
                    document.getElementById('container').innerHTML = '<h1>Dynamic Title</h1><p>This content was rendered by JavaScript</p>';
                </script>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("js-rendered.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then - should contain JS-rendered content, not "Loading..."
        assertContains(markdown, "# Dynamic Title")
        assertContains(markdown, "This content was rendered by JavaScript")
        assert(!markdown.contains("Loading..."))
    }

    @Test
    fun `should preserve unicode characters`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body>
                <h1>Cześć świecie</h1>
                <p>日本語テキスト with emoji 🎉 and symbols © ® ™</p>
                <p>Greek: αβγδ, Cyrillic: абвг, Arabic: مرحبا</p>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("unicode.html")
        Files.writeString(htmlFile, html, Charsets.UTF_8)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.open("file://$htmlFile")

        // then
        assertContains(markdown, "Cześć świecie")
        assertContains(markdown, "日本語テキスト")
        assertContains(markdown, "🎉")
        assertContains(markdown, "αβγδ")
        assertContains(markdown, "абвг")
        assertContains(markdown, "مرحبا")
    }

    // Session-based tests

    @Test
    fun `should open URL in session`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Session Page</h1>
                <p>Content in session</p>
            </body>
            </html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("session-page.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        val markdown = webBrowser.openInSession("test-session", "file://$htmlFile")

        // then
        assertContains(markdown, "# Session Page")
        assertContains(markdown, "Content in session")

        // cleanup
        webBrowser.closeSession("test-session")
    }

    @Test
    fun `should list active sessions`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html><body><p>Test</p></body></html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("session-list.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)

        // when
        webBrowser.openInSession("session-a", "file://$htmlFile")
        webBrowser.openInSession("session-b", "file://$htmlFile")

        // then
        val sessions = webBrowser.listSessions()
        assert(sessions.contains("session-a"))
        assert(sessions.contains("session-b"))
        assert(sessions.size == 2)

        // cleanup
        webBrowser.closeSession("session-a")
        webBrowser.closeSession("session-b")
    }

    @Test
    fun `should close session and remove from list`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html><body><p>Test</p></body></html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("session-close.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)
        webBrowser.openInSession("to-close", "file://$htmlFile")
        assert(webBrowser.listSessions().contains("to-close"))

        // when
        webBrowser.closeSession("to-close")

        // then
        assert(!webBrowser.listSessions().contains("to-close"))
    }

    @Test
    fun `should handle closing non-existent session gracefully`() = runTest {
        // given
        val webBrowser = DefaultWebBrowser(browser)

        // when/then - should not throw
        webBrowser.closeSession("non-existent")
    }

    @Test
    fun `should persist state within session using localStorage`() = runTest {
        // given - page that sets localStorage (works better than cookies for file:// URLs)
        val setStoragePage = """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Storage Setter</h1>
                <script>localStorage.setItem('testkey', 'sessionvalue');</script>
            </body>
            </html>
        """.trimIndent()

        // page that reads localStorage
        val readStoragePage = """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Storage Reader</h1>
                <p id="storage-value"></p>
                <script>document.getElementById('storage-value').textContent = localStorage.getItem('testkey') || 'NO_VALUE';</script>
            </body>
            </html>
        """.trimIndent()

        val setStorageFile = tempDir.resolve("set-storage.html")
        val readStorageFile = tempDir.resolve("read-storage.html")
        Files.writeString(setStorageFile, setStoragePage)
        Files.writeString(readStorageFile, readStoragePage)

        val webBrowser = DefaultWebBrowser(browser)

        // when - set localStorage in session
        webBrowser.openInSession("storage-session", "file://$setStorageFile")

        // then - read localStorage in same session (should persist)
        val result = webBrowser.openInSession("storage-session", "file://$readStorageFile")
        assertContains(result, "sessionvalue")

        // cleanup
        webBrowser.closeSession("storage-session")
    }

    @Test
    fun `should isolate sessions from each other`() = runTest {
        // given - page that sets localStorage
        val setStoragePage = """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Storage Setter</h1>
                <script>localStorage.setItem('isolated', 'session-a-value');</script>
            </body>
            </html>
        """.trimIndent()

        // page that reads localStorage
        val readStoragePage = """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Storage Reader</h1>
                <p id="storage-value"></p>
                <script>document.getElementById('storage-value').textContent = localStorage.getItem('isolated') || 'NO_VALUE';</script>
            </body>
            </html>
        """.trimIndent()

        val setStorageFile = tempDir.resolve("set-isolated.html")
        val readStorageFile = tempDir.resolve("read-isolated.html")
        Files.writeString(setStorageFile, setStoragePage)
        Files.writeString(readStorageFile, readStoragePage)

        val webBrowser = DefaultWebBrowser(browser)

        // when - set localStorage in session-a
        webBrowser.openInSession("session-a", "file://$setStorageFile")

        // then - session-b should NOT see the localStorage
        val resultB = webBrowser.openInSession("session-b", "file://$readStorageFile")
        assert(!resultB.contains("session-a-value")) {
            "Session B should not see Session A's localStorage"
        }

        // cleanup
        webBrowser.closeSession("session-a")
        webBrowser.closeSession("session-b")
    }

    @Test
    fun `should not share state between stateless open and session`() = runTest {
        // given - page that sets localStorage
        val setStoragePage = """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Storage Setter</h1>
                <script>localStorage.setItem('stateless', 'test-value');</script>
            </body>
            </html>
        """.trimIndent()

        val readStoragePage = """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Storage Reader</h1>
                <p id="storage-value"></p>
                <script>document.getElementById('storage-value').textContent = localStorage.getItem('stateless') || 'NO_VALUE';</script>
            </body>
            </html>
        """.trimIndent()

        val setStorageFile = tempDir.resolve("set-stateless.html")
        val readStorageFile = tempDir.resolve("read-stateless.html")
        Files.writeString(setStorageFile, setStoragePage)
        Files.writeString(readStorageFile, readStoragePage)

        val webBrowser = DefaultWebBrowser(browser)

        // when - set localStorage via stateless open
        webBrowser.open("file://$setStorageFile")

        // then - session should not see it (stateless pages are isolated)
        val result = webBrowser.openInSession("fresh-session", "file://$readStorageFile")
        assert(!result.contains("test-value")) {
            "Session should not see localStorage from stateless open"
        }

        // cleanup
        webBrowser.closeSession("fresh-session")
    }

    @Test
    fun `should clean up all sessions on close`() = runTest {
        // given
        val html = """
            <!DOCTYPE html>
            <html><body><p>Test</p></body></html>
        """.trimIndent()

        val htmlFile = tempDir.resolve("cleanup.html")
        Files.writeString(htmlFile, html)

        val webBrowser = DefaultWebBrowser(browser)
        webBrowser.openInSession("cleanup-1", "file://$htmlFile")
        webBrowser.openInSession("cleanup-2", "file://$htmlFile")
        assert(webBrowser.listSessions().size == 2)

        // when
        webBrowser.close()

        // then
        assert(webBrowser.listSessions().isEmpty())
    }

}
