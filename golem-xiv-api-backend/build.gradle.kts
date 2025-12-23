import groovy.json.StringEscapeUtils

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("golem.convention")
}

val generatedSourcesDir = layout.buildDirectory.dir("generated/source/main/kotlin")

kotlin {
    sourceSets {
        main {
            kotlin.srcDir(generatedSourcesDir)
        }
    }
}

dependencies {
    api(project(":golem-xiv-api"))
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)
    api(libs.xemantic.neo4j.kotlin.driver)
    api(libs.xemantic.ai.tool.schema)
    api(libs.ktor.client.core)
    api(libs.markanywhere.api)

    implementation(project(":golem-xiv-json")) //

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
}

// Define the task to generate the Kotlin source
tasks.register("generateGolemScriptApi") {

    val sourceFile = "src/main/kotlin/script/GolemScriptApi.kt"
    val packageName = "com.xemantic.ai.golem.api.backend.script"

    inputs.file(sourceFile)
    outputs.dir(generatedSourcesDir)

    doLast {
        generatedSourcesDir.get().asFile.mkdirs()

        // Read the source file
        val sourceInput = file(sourceFile).readText().substringAfter("*/")
        val sourceContent: String = StringEscapeUtils.escapeJava(
            sourceInput
        ).replace("$", "${'$'}{'$'}")

        // Generate the Kotlin file with the source as a string constant
        file("${generatedSourcesDir.get()}/GeneratedGolemScriptApi.kt").writeText("""
            package $packageName

            const val GOLEM_SCRIPT_API = "$sourceContent"
        """.trimIndent())
    }
}

// Make sure the source is generated before compilation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateGolemScriptApi")
}
