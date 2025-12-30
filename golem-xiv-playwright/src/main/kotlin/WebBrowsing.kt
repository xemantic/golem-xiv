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

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.util.data.MutableDataSet
import com.xemantic.ai.golem.api.backend.script.incubation.WebBrowser

class DefaultWebBrowser(
    val browser: Browser
) : WebBrowser {

    private val page by lazy { browser.newPage() }

    override suspend fun open(url: String): String {
        page.navigate(url, Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
        val html = page.content()
        val markdown = convertHtmlToMarkdown(html)
        //page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
        //page.waitForTimeout(200.0)
        return markdown
    }


//    override suspend fun screenshot(): ByteArray {
////        page.screenshot(
////            Page.ScreenshotOptions()
////                .setPath(Paths.get("screenshot.png"))
////                .setFullPage(true)
////        )
//        return byteArrayOf()
//    }

//    override fun close() {
//
//    }
//    override fun dispose() {
//        TODO("Not yet implemented")
//    }

}

private fun convertHtmlToMarkdown(html: String): String {
    val options = MutableDataSet()
    val converter = FlexmarkHtmlConverter.builder(options).build()
    return converter.convert(html)
}
