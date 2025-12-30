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
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    implementation(libs.neo4j)
}

tasks.register<JavaExec>("runNeo4j") {
    description = "Start Neo4j embedded database with optimized JVM settings"
    group = "golem"

    standardInput = System.`in`
    standardOutput = System.out
    errorOutput = System.err

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.xemantic.ai.golem.neo4j.starter.GolemNeo4jKt")

    jvmArgs("-Xms512m", "-Xmx1g")

    // Make task interruptible
    doFirst {
        println("Starting Neo4j (DEV MODE)")
        println("Press Ctrl+C to stop")
    }
}
