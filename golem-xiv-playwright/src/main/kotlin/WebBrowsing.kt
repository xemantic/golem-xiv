/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
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
