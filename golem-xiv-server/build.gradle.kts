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

dependencies {
    implementation(libs.playwright)
    implementation(libs.anthropic.sdk.kotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.jvm.host)
    //implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.10")
    implementation("com.microsoft.playwright:playwright:1.50.0")

    testImplementation(libs.kotlin.test)
    testImplementation(libs.xemantic.kotlin.test)
}

//powerAssert {
//    functions = listOf(
//        "com.xemantic.kotlin.test.assert",
//        "com.xemantic.kotlin.test.have"
//    )
//}

// Define the task to generate the Kotlin source
tasks.register("generateToolsApi") {
    val sourceFile = "src/commonMain/kotlin/service/GolemScriptServiceApi.kt"
    val outputDir = "build/generated/source/golemScriptServiceApi"
    val packageName = "com.xemantic.ai.golem"

    inputs.file(sourceFile)
    outputs.dir(outputDir)

    doLast {
        // Create output directory
        val outputPath = "$outputDir/${packageName.replace('.', '/')}"
        mkdir(outputPath)

        // Read the source file
        val sourceContent = file(sourceFile).readText()
            .substringAfter("*/")
            .replace("\"", "\\\"") // Escape quotes
            .replace("\n", "\\n") // Handle newlines

        // Generate Kotlin file with the source as a string constant
        file("$outputPath/GeneratedGolemScriptServiceApi.kt").writeText("""
            package $packageName

            const val GOLEM_SCRIPT_SERVICE_API = "$sourceContent"
        """.trimIndent())
    }
}

// Make sure the source is generated before compilation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateToolsApi")
}
