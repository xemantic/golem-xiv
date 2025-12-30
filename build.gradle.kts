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
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
//    alias(libs.plugins.kotlin.plugin.serialization)
//    alias(libs.plugins.kotlin.plugin.power.assert)
////    alias(libs.plugins.kotlinx.binary.compatibility.validator)
//    alias(libs.plugins.dokka)
    alias(libs.plugins.versions)
    alias(libs.plugins.xemantic.conventions)
}

// TODO change the group
group = "com.xemantic.ai.golem"

//// TODO fill up the details
xemantic {
    description = "An autonomous metacognitive AI system"
    inceptionYear = "2025"
    applyAxTestReporting()
}


//val javaTarget = libs.versions.javaTarget.get()
//val kotlinTarget = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())


allprojects {
    repositories {
        mavenCentral()
    }

//    tasks.withType<AbstractArchiveTask> {
//        // Only set if not already set
//        if (!archiveBaseName.isPresent) {
//            archiveBaseName.set(project.name)
//        }
//        archiveVersion.set(project.version.toString())
//    }
//    apply(plugin = "maven-publish")
}

tasks.register<Delete>("cleanDevStorage") {
    group = "golem"
    description = "Cleans local dev neo4j and disk storage"
    delete(fileTree("var/neo4j/data"))
    delete(fileTree("var/neo4j/logs"))
    delete(fileTree("var/cognitions"))
}
