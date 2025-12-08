import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor.plugin)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("golem.convention")
}

application {
    mainClass = "com.xemantic.ai.golem.server.GolemServerKt"
}

dependencies {
    implementation(project(":golem-xiv-logging"))
    implementation(project(":golem-xiv-api"))
    implementation(project(":golem-xiv-api-websocket"))
    implementation(project(":golem-xiv-api-backend"))
    implementation(project(":golem-xiv-neo4j"))
    implementation(project(":golem-xiv-cognizer-anthropic"))
    implementation(project(":golem-xiv-playwright"))
    implementation(project(":golem-xiv-core"))

    implementation(libs.kotlinx.serialization.core)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.jvm.host)

    implementation(libs.kotlin.logging)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.ktor.client.java)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.anthropic.sdk.kotlin)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
    testImplementation(libs.neo4j.harness)


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

tasks.withType<KotlinCompile> {
    doFirst {
        println("Kotlin JVM target: ${compilerOptions.jvmTarget.get()}")
        println("Kotlin language version: ${compilerOptions.languageVersion.get()}")
        println("Kotlin API version: ${compilerOptions.apiVersion.get()}")
        println("Using JDK: ${System.getProperty("java.home")}")
        println("Kotlin compiler args: ${compilerOptions.freeCompilerArgs.get()}")
    }
}