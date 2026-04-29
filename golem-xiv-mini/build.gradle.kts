/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2025-2026  Kazimierz Pogoda / Xemantic
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

plugins {
    alias(libs.plugins.kotlin.jvm)
//    alias(libs.plugins.ktor.plugin)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("golem.convention")
}

//application {
//    mainClass = "com.xemantic.ai.golem.server.GolemServerKt"
//}

dependencies {
    implementation(project(":golem-xiv-logging"))
    implementation(project(":golem-xiv-api"))
    implementation(project(":golem-xiv-core"))

    implementation(libs.kotlinx.serialization.core)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.jvm.host)

    implementation(libs.kotlin.logging)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.ktor.client.java)
    implementation(libs.ktor.client.content.negotiation)
    // cors is needed only during development
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.anthropic.sdk.kotlin)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
}


//tasks.named("shadowJar") {
//    dependsOn("copyWebResources")
//}

//ktor {
//    fatJar {
//        archiveFileName.set("golem-xiv-server-${project.version}-all.jar")
//    }
//}
//
//listOf(
//    "distTar",
//    "distZip",
//    "startScripts",
//    "startShadowScripts",
//    "shadowDistTar",
//    "shadowDistZip",
//    "assemble"
//).forEach {
//    tasks.named(it) {
//        enabled = false
//    }
//}
