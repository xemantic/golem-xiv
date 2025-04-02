@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.plugin.power.assert)
}

kotlin {

    compilerOptions {
        //apiVersion = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())
        //languageVersion = kotlinTarget
        freeCompilerArgs.addAll(
            "-Xmulti-dollar-interpolation",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi"
        )
        extraWarnings = true
        progressiveMode = true
    }

    js {
        browser {
        }
        binaries.executable()
    }

    sourceSets {

        commonMain {
            dependencies {
                implementation(project(":golem-xiv-api"))
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.xemantic.kotlin.test)
            }
        }

        jsMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.html)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }

    }

}

powerAssert {
    functions = listOf(
        "com.xemantic.kotlin.test.assert",
        "com.xemantic.kotlin.test.have"
    )
}
