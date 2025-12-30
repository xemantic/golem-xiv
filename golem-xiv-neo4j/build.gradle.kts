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
    alias(libs.plugins.kotlin.plugin.serialization)
    id("golem.convention")
}

dependencies {
    api(project(":golem-xiv-api-backend"))
    implementation(project(":golem-xiv-logging"))

    api(libs.neo4j.java.driver)
    api(libs.xemantic.neo4j.kotlin.driver)

    implementation(libs.neo4j.migrations)
    implementation(libs.logback.classic)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
    testImplementation(libs.neo4j.harness) {
        // we are running tests involving both - the driver and neo4j instance itself
        // for this reason we need to keep only one logging bindings provider (logback)
        exclude(group = "org.neo4j",  module = "neo4j-slf4j-provider")
    }
}
