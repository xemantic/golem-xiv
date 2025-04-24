/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.ai.golem.server.script.service

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.util.data.MutableDataSet
import com.xemantic.ai.golem.server.script.candidate.WebBrowser

class DefaultWebBrowser(
    val browser: Browser
) : WebBrowser {

    private val page by lazy { browser.newPage() }

    override suspend fun open(
        url: String,
        windowId: Int?
    ): String {
        page.navigate(url, Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
        val html = page.content()
        val markdown = convertHtmlToMarkdown(html)
        //page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
        //page.waitForTimeout(200.0)
        return markdown
    }

    override suspend fun openAsBinary(url: String, windowId: Int?): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun screenshot(): ByteArray {
//        page.screenshot(
//            Page.ScreenshotOptions()
//                .setPath(Paths.get("screenshot.png"))
//                .setFullPage(true)
//        )
        return byteArrayOf()
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

}

private fun convertHtmlToMarkdown(html: String): String {
    val options = MutableDataSet()
    val converter = FlexmarkHtmlConverter.builder(options).build()
    return converter.convert(html)
}
