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

package com.xemantic.ai.golem.server.service

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.WaitUntilState
import java.util.concurrent.ConcurrentHashMap

class WebBrowserServiceRegistry {

    private val playwright = Playwright.create()!!

    private val serviceMap = ConcurrentHashMap<String, WebBrowserService>()

    fun get(conversationId: String): WebBrowserService = serviceMap.computeIfAbsent(
        conversationId
    ) {
        DefaultWebBrowserService()
    }

    inner class DefaultWebBrowserService : WebBrowserService {

        val browser = playwright.chromium().launch(
            BrowserType.LaunchOptions()
                .setHeadless(false)
        )!!

        override suspend fun openUrl(url: String, windowId: Int?): Content {
            Playwright.create().use { playwright ->
                val page = browser.newPage()
                page.navigate(url, Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
                page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
                page.waitForTimeout(200.0)
                // TODO wait again for the network
            }
            return Content.Text("done")
        }

        override suspend fun screenshot(): Content.Binary {
//        page.screenshot(
//            Page.ScreenshotOptions()
//                .setPath(Paths.get("screenshot.png"))
//                .setFullPage(true)
//        )
            return Content.Binary(byteArrayOf())
        }

    }

}
