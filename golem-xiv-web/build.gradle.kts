import com.github.gradle.node.yarn.task.YarnTask
import java.net.URI

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.node.gradle)
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
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }

    }

}

node {
    download = true
    nodeProjectDir = file("build/neo4j-browser")
}

val neo4jBrowserVersion: String = libs.versions.neo4jBrowser.get()

tasks.register<Exec>("cloneNeo4jBrowser") {
    group = "golem-support"
    description = "Clones neo4j-browser repository, builds it, and copies the build to jsMain resources"
    val cloneDir = project.layout.buildDirectory.get().asFile
    File(cloneDir, "neo4j-browser").deleteRecursively()
    cloneDir.mkdirs()
    workingDir = project.layout.buildDirectory.get().asFile
    commandLine = "git -c advice.detachedHead=false clone --branch $neo4jBrowserVersion --depth 1 https://github.com/neo4j/neo4j-browser.git".split(' ')
}

tasks.register<YarnTask>("neo4jBrowserYarnInstall") {
    group = "golem-support"
    dependsOn("cloneNeo4jBrowser")
    args.set(listOf("install"))
}

tasks.register<YarnTask>("neo4jBrowserYarnBuild") {
    group = "golem-support"
    dependsOn("neo4jBrowserYarnInstall")
    environment = mapOf("NODE_OPTIONS" to "--openssl-legacy-provider")
    args.set(listOf("build"))
}

tasks.register("installNeo4jBrowser") {
    group = "golem"
    dependsOn("neo4jBrowserYarnBuild")
    val destination = file("src/jsMain/resources/neo4j-browser")
    destination.mkdirs()
    copy {
        from("build/neo4j-browser/dist")
        into(destination)
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
