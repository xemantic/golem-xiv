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

package com.xemantic.ai.golem.service

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual fun webBrowserService(): WebBrowserService = JvmWebBrowserService()

class JvmWebBrowserService : WebBrowserService {

    val playwright = Playwright.create()!!

    val browser = playwright.chromium().launch(
        BrowserType.LaunchOptions()
            .setHeadless(false)
    )!!

    suspend fun navigateAndWait(url: String) = suspendCoroutine { continuation ->
        Playwright.create().use { playwright ->
            val page = browser.newPage()

            page.navigate(url, Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))

            // Do something with the page
            val title = page.title()

            continuation.resume(title)
        }
    }

    override suspend fun openUrl(url: String, windowId: Int?): Content {
        TODO("Not yet implemented")
    }

    override suspend fun screenshot(): Content.Binary {
        TODO("Not yet implemented")
    }

}
