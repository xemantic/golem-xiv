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

package com.xemantic.ai.golem.server.os

import com.xemantic.ai.golem.api.OsProcessEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.awt.Desktop
import java.io.BufferedReader
import java.net.URI
import java.io.IOException
import java.io.InputStreamReader

fun operatingSystemName(): String = System.getProperty("os.name")!!

fun openBrowser(url: String) {
    val os = System.getProperty("os.name").lowercase()
    try {
        when {
            Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) -> {
                Desktop.getDesktop().browse(URI(url))
            }
            "linux" in os -> {
                // For Linux/Unix
                val browsers = arrayOf("xdg-open", "google-chrome", "firefox", "mozilla", "konqueror", "netscape", "opera", "epiphany")
                val command = browsers.firstOrNull { Runtime.getRuntime().exec(arrayOf("which", it)).waitFor() == 0 }
                if (command != null) {
                    Runtime.getRuntime().exec(arrayOf(command, url))
                } else {
                    throw Exception("No known browser found")
                }
            }
            "mac" in os -> {
                // For macOS
                Runtime.getRuntime().exec(arrayOf("open", url))
            }
            "win" in os -> {
                // For Windows
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler $url")
            }
            else -> throw Exception("Unsupported operating system")
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun executeProcess(command: List<String>): Flow<OsProcessEvent> = flow {
    val processBuilder = ProcessBuilder(command)

    // Redirect stderr to stdout
    processBuilder.redirectErrorStream(true)

    val process = processBuilder.start()

    // Read the output line by line
    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            line?.let {
                emit(OsProcessEvent.Output(it))
            }
        }
    }

    // Wait for the process to complete and get the exit code
    val exitCode = process.waitFor()
    emit(OsProcessEvent.Exit(exitCode))
} //.flowOn(Dispatchers.IO)
