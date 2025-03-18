plugins {
    alias(libs.plugins.kotlin.jvm)
//    alias(libs.plugins.kotlin.plugin.serialization)
//    alias(libs.plugins.kotlin.plugin.power.assert)
//    alias(libs.plugins.dokka)
//    alias(libs.plugins.versions)
//    `maven-publish`
//    signing
//    alias(libs.plugins.jreleaser)
}

//    compilerOptions {
//        apiVersion = kotlinTarget
//        languageVersion = kotlinTarget
//        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
//        extraWarnings = true
//        progressiveMode = true
//    }

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

kotlin {
    sourceSets {
        main {
            kotlin.srcDir(generatedSourcesDir)
        }
    }
}

dependencies {
    implementation(libs.playwright)
    implementation(libs.anthropic.sdk.kotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.jvm.host)

    implementation(libs.ktor.client.java)
    //implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.10")
    implementation(libs.playwright)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.xemantic.kotlin.test)

    implementation("com.vladsch.flexmark:flexmark:0.64.8")
    implementation("com.vladsch.flexmark:flexmark-html2md-converter:0.64.8")
}

//powerAssert {
//    functions = listOf(
//        "com.xemantic.kotlin.test.assert",
//        "com.xemantic.kotlin.test.have"
//    )
//}

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
