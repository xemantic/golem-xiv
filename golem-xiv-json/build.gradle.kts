@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.power.assert)
}

//    jvm {
//        // set up according to https://jakewharton.com/gradle-toolchains-are-rarely-a-good-idea/
////        compilerOptions {
////            apiVersion = kotlinTarget
////            languageVersion = kotlinTarget
////            jvmTarget = JvmTarget.fromTarget(javaTarget)
////            freeCompilerArgs.add("-Xjdk-release=$javaTarget")
////            progressiveMode = true
////        }
//    }

kotlin {
    compilerOptions {
        //apiVersion = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())
        //languageVersion = kotlinTarget
        freeCompilerArgs.addAll(
            "-Xmulti-dollar-interpolation",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlin.time.ExperimentalTime"
        )
        extraWarnings = true
        progressiveMode = true
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.xemantic.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

powerAssert {
    functions = listOf(
        "com.xemantic.kotlin.test.assert",
        "com.xemantic.kotlin.test.have"
    )
}
