@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor.plugin)
//    alias(libs.plugins.kotlin.plugin.serialization)
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

val generatedSourcesDir = layout.buildDirectory.dir("generated/source/main/kotlin")

application {
    mainClass = "com.xemantic.ai.golem.server.GolemServerKt"
}

kotlin {

    compilerOptions {
        //apiVersion = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())
        //languageVersion = kotlinTarget
        freeCompilerArgs.addAll(
            "-Xmulti-dollar-interpolation",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
        extraWarnings = true
        progressiveMode = true
    }

    sourceSets {
        main {
            kotlin.srcDir(generatedSourcesDir)
        }
    }
}

dependencies {
    implementation(project(":golem-xiv-api"))
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.jvm.host)

    implementation(libs.kotlin.logging)

    implementation(libs.anthropic.sdk.kotlin)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.ktor.client.java)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.playwright)

    runtimeOnly(libs.log4j.slf4j2)
    runtimeOnly(libs.jackson.databind)
    runtimeOnly(libs.jackson.dataformat.yaml)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.xemantic.kotlin.test)

    implementation("com.vladsch.flexmark:flexmark:0.64.8")
    implementation("com.vladsch.flexmark:flexmark-html2md-converter:0.64.8")
}

powerAssert {
    functions = listOf(
        "com.xemantic.kotlin.test.assert",
        "com.xemantic.kotlin.test.have"
    )
}

// Define the task to generate the Kotlin source
tasks.register("generateGolemScriptApi") {
    val sourceFile = "src/main/kotlin/script/GolemScriptApi.kt"

    val packageName = "com.xemantic.ai.golem.server.script"

    inputs.file(sourceFile)
    outputs.dir(generatedSourcesDir)

    doLast {
        generatedSourcesDir.get().asFile.mkdirs()

        // Read the source file
        val sourceContent = file(sourceFile).readText()
            .substringAfter("*/")
            .replace("\"", "\\\"") // Escape quotes
            .replace("\n", "\\n") // Handle newlines

        // Generate Kotlin file with the source as a string constant
        file("${generatedSourcesDir.get()}/GeneratedGolemServiceApi.kt").writeText("""
            package $packageName

            const val GOLEM_SCRIPT_API = "$sourceContent"
        """.trimIndent())
    }
}

// Make sure the source is generated before compilation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateGolemScriptApi")
}

tasks.register<Copy>("copyWebResources") {
    dependsOn(":golem-xiv-web:jsBrowserDistribution")
    from(
        project(":golem-xiv-web")
            .layout.buildDirectory
            .dir("dist/js/productionExecutable")
    ) {
        exclude("index.html")
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
