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

plugins {
    `kotlin-dsl`
}

val javaTarget = libs.versions.javaTarget.get()

java {
    val version = JavaVersion.toVersion(javaTarget)
    sourceCompatibility = version
    targetCompatibility = version
}

tasks.compileJava {
    options.release = javaTarget.toInt()
}

kotlin {
    compilerOptions {
        val specifiedJvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaTarget)
        jvmTarget = specifiedJvmTarget
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.power.assert)
}

gradlePlugin {
    plugins {
        register("GolemConventionPlugin") {
            id = "golem.convention"
            implementationClass = "con.xemantic.ai.golem.buildlogic.GolemConventionPlugin"
        }
    }
}
