import com.github.gradle.node.yarn.task.YarnTask

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

tasks.register<Exec>("cloneNeo4JBrowser") {
    group = "neo4j browser"
    description = "Clones neo4j-browser repository, builds it, and copies the build to jsMain resources"
    val cloneDir = project.layout.buildDirectory.get().asFile
    File(cloneDir, "neo4j-browser").deleteRecursively()
    cloneDir.mkdirs()
    workingDir = project.layout.buildDirectory.get().asFile
    commandLine = "git -c advice.detachedHead=false clone --branch $neo4jBrowserVersion --depth 1 https://github.com/neo4j/neo4j-browser.git".split(' ')
}

tasks.register<YarnTask>("neo4JBrowserYarnInstall") {
    group = "neo4j browser"
    dependsOn("cloneNeo4JBrowser")
    args.set(listOf("install"))
}

tasks.register<YarnTask>("neo4JBrowserYarnBuild") {
    group = "neo4j browser"
    dependsOn("neo4JBrowserYarnInstall")
    environment = mapOf("NODE_OPTIONS" to "--openssl-legacy-provider")
    args.set(listOf("build"))
}

tasks.register("installNeo4JBrowser") {
    group = "neo4j browser"
    dependsOn("neo4JBrowserYarnBuild")
    val destination = file("src/jsMain/resources/neo4j-browser")
    destination.mkdirs()
    copy {
        from("build/neo4j-browser/dist")
        into(destination)
    }
}
