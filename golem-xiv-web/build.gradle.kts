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

import java.net.URI
import java.security.MessageDigest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("golem.convention")
}

kotlin {

    js {
        browser()
        binaries.executable()
    }

    sourceSets {

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.xemantic.kotlin.test)
            }
        }

        jsMain {
            dependencies {
                implementation(project(":golem-xiv-api"))
                implementation(project(":golem-xiv-presenter"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlin.logging)
                implementation(libs.kotlinx.html)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }

    }

}

val neo4jBrowserVersion: String = libs.versions.neo4jBrowser.get()
val neo4jBrowserSha256: String = libs.versions.neo4jBrowserSha256.get()

tasks.register("installNeo4jBrowser") {
    group = "golem"
    description = "Downloads pre-built Neo4j Browser and installs it to jsMain resources"
    val tarballUrl = "https://github.com/neo4j/neo4j-browser/releases/download/$neo4jBrowserVersion/neo4j-browser-$neo4jBrowserVersion.tgz"
    val downloadDir = project.layout.buildDirectory.dir("neo4j-browser").get().asFile
    val tarballFile = File(downloadDir, "neo4j-browser-$neo4jBrowserVersion.tgz")
    val destination = file("src/jsMain/resources/neo4j-browser")

    inputs.property("neo4jBrowserVersion", neo4jBrowserVersion)
    inputs.property("neo4jBrowserSha256", neo4jBrowserSha256)
    outputs.dir(destination)

    doLast {
        downloadDir.mkdirs()
        if (!tarballFile.exists()) {
            println("Downloading Neo4j Browser $neo4jBrowserVersion...")
            try {
                download(tarballUrl, tarballFile.absolutePath)
            } catch (e: Exception) {
                throw GradleException("Failed to download Neo4j Browser: ${e.message}", e)
            }
            val actualSha256 = sha256(tarballFile)
            if (actualSha256 != neo4jBrowserSha256) {
                tarballFile.delete()
                throw GradleException(
                    "Neo4j Browser checksum mismatch: expected $neo4jBrowserSha256, got $actualSha256"
                )
            }
        }
        destination.deleteRecursively()
        println("Extracting Neo4j Browser to $destination...")
        try {
            copy {
                from(tarTree(tarballFile)) {
                    include("*/dist/**")
                    eachFile {
                        // Strip "{root}/dist/" prefix dynamically
                        val distIndex = relativePath.segments.indexOf("dist")
                        if (distIndex >= 0) {
                            relativePath = RelativePath(true, *relativePath.segments.drop(distIndex + 1).toTypedArray())
                        }
                    }
                    includeEmptyDirs = false
                }
                into(destination)
            }
        } catch (e: Exception) {
            tarballFile.delete()
            throw GradleException("Failed to extract Neo4j Browser: ${e.message}", e)
        }
        if (!File(destination, "index.html").exists()) {
            destination.deleteRecursively()
            throw GradleException("Failed to install Neo4j Browser: index.html not found after extraction")
        }
        println("Neo4j Browser $neo4jBrowserVersion installed successfully")
    }
}

tasks.register("updateBeerCSS") {
    group = "web"
    description = "Checks for latest BeerCSS version and downloads CSS and JS files to local resources"

    val cssFolder = "src/jsMain/resources/css"
    val cssFile = file("$cssFolder/beercss.css")
    val jsFile = file("$cssFolder/beercss.js")
    val svgFile = file("$cssFolder/loading-indicator.svg")

    outputs.files(cssFile, jsFile, svgFile)

    doLast {
        // Check latest version from npm registry
        val npmApiUrl = "https://registry.npmjs.org/beercss/latest"
        val tempJsonFile = File.createTempFile("beercss-version", ".json")
        val latestVersionJson = try {
            download(npmApiUrl, tempJsonFile.absolutePath)
            tempJsonFile.readText()
        } catch (e: Exception) {
            println("Failed to fetch latest BeerCSS version: ${e.message}")
            return@doLast
        } finally {
            tempJsonFile.delete()
        }

        // Extract version from JSON response
        val versionPattern = """"version"\s*:\s*"([0-9]+\.[0-9]+\.[0-9]+)"""".toRegex()
        val latestVersionMatch = versionPattern.find(latestVersionJson)

        if (latestVersionMatch == null) {
            println("Could not parse latest version from npm registry response")
            return@doLast
        }

        val latestVersion = latestVersionMatch.groupValues[1]
        println("Latest BeerCSS version: $latestVersion")

        // Download CSS file
        val cssUrl = "https://cdn.jsdelivr.net/npm/beercss@$latestVersion/dist/cdn/beer.min.css"
        println("Downloading CSS from: $cssUrl")
        try {
            download(cssUrl, cssFile.absolutePath)
            println("Successfully downloaded beercss.css")
        } catch (e: Exception) {
            println("Failed to download CSS file: ${e.message}")
            return@doLast
        }

        // Remove @font-face declarations from CSS file
        try {
            val cssContent = cssFile.readText()
            val fontFacePattern = """@font-face\s*\{[^}]*\}""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val cleanedCssContent = cssContent.replace(fontFacePattern, "")
            cssFile.writeText(cleanedCssContent)
            println("Removed @font-face declarations from CSS file")
        } catch (e: Exception) {
            println("Failed to remove @font-face declarations: ${e.message}")
        }

        // Download JS file
        val jsUrl = "https://cdn.jsdelivr.net/npm/beercss@$latestVersion/dist/cdn/beer.min.js"
        println("Downloading JS from: $jsUrl")
        try {
            download(jsUrl, jsFile.absolutePath)
            println("Successfully downloaded beercss.js")
        } catch (e: Exception) {
            println("Failed to download JS file: ${e.message}")
            return@doLast
        }

        // Download loading-indicator.svg file
        val svgUrl = "https://cdn.jsdelivr.net/npm/beercss@$latestVersion/dist/cdn/loading-indicator.svg"
        println("Downloading SVG from: $svgUrl")
        try {
            download(svgUrl, svgFile.absolutePath)
            println("Successfully downloaded loading-indicator.svg")
        } catch (e: Exception) {
            println("Failed to download SVG file: ${e.message}")
            return@doLast
        }

        println("BeerCSS successfully updated to version $latestVersion")
    }
}

fun download(
    url: String,
    to: String
) {
    val url = URI(url).toURL()
    val outputFile = File(to)
    outputFile.parentFile.mkdirs()
    url.openStream().use { input ->
        outputFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
