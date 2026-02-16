/*
 * Golem XIV - Autonomous metacognitive AI system with semantic memory and self-directed research
 * Copyright (C) 2026  Kazimierz Pogoda / Xemantic
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

import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.js.plain.objects)
    id("golem.convention")
}

kotlin {
    js {
        browser()
        binaries.executable()
        useEsModules()
        compilerOptions {
            moduleKind.set(JsModuleKind.MODULE_ES)
            target = "es2015"
            freeCompilerArgs.addAll(
                "-Xir-generate-inline-anonymous-functions",
                "-Xir-minimized-member-names",
                "-Xir-dce",
                "-Xoptimize-generated-js",
                "-Xes-arrow-functions"
            )
            optIn.addAll(
                "kotlin.js.ExperimentalJsExport",
                "kotlin.js.ExperimentalJsCollectionsApi"
            )
        }
    }

    sourceSets {

        jsMain {
            dependencies {
                implementation(libs.xemantic.kotlin.js)
            }
        }

        jsTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.xemantic.kotlin.test)
                implementation(libs.markanywhere.test)
            }
        }

    }

}
