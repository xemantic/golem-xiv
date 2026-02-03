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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor.plugin)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("golem.convention")
}

application {
    mainClass = "com.xemantic.ai.golem.server.GolemServerKt"
}

dependencies {
    implementation(project(":golem-xiv-logging"))
    implementation(project(":golem-xiv-api"))
    implementation(project(":golem-xiv-api-backend"))
    implementation(project(":golem-xiv-neo4j"))
    implementation(project(":golem-xiv-cognizer-anthropic"))
    implementation(project(":golem-xiv-playwright"))
    implementation(project(":golem-xiv-core"))

    implementation(libs.kotlinx.serialization.core)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.jvm.host)

    implementation(libs.kotlin.logging)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.ktor.client.java)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.sse)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    // cors is needed only during development
    // TODO check how to exclude it when assembling shadowJar
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.anthropic.sdk.kotlin)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
    testImplementation(libs.neo4j.harness)
    testImplementation(libs.ktor.server.test.host)
}

tasks.register<Copy>("copyWebResources") {
    dependsOn(":golem-xiv-web:jsBrowserDistribution")
    from(
        project(":golem-xiv-web")
            .layout.buildDirectory
            .dir("dist/js/productionExecutable")
    ) {
        filter { line ->
            if (line.contains("golem-dev-script")) null else line
        }
    }
    into(
        layout.buildDirectory.dir("resources/main/web")
    )
}

// Make sure the copy task runs before the server's resources are processed
tasks.named("jar") {
    dependsOn("copyWebResources")
}

tasks.named("shadowJar") {
    dependsOn("copyWebResources")
}

ktor {
    fatJar {
        archiveFileName.set("golem-xiv-server-${project.version}-all.jar")
    }
}

listOf(
    "distTar",
    "distZip",
    "startScripts",
    "startShadowScripts",
    "shadowDistTar",
    "shadowDistZip",
    "assemble"
).forEach {
    tasks.named(it) {
        enabled = false
    }
}

tasks.withType<KotlinCompile> {
    doFirst {
        println("Kotlin JVM target: ${compilerOptions.jvmTarget.get()}")
        println("Kotlin language version: ${compilerOptions.languageVersion.get()}")
        println("Kotlin API version: ${compilerOptions.apiVersion.get()}")
        println("Using JDK: ${System.getProperty("java.home")}")
        println("Kotlin compiler args: ${compilerOptions.freeCompilerArgs.get()}")
    }
}