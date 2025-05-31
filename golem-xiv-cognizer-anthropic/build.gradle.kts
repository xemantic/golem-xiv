plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    api(project(":golem-xiv-api"))
    api(project(":golem-xiv-api-backend"))
    api(libs.anthropic.sdk.kotlin)
    api(libs.kotlinx.coroutines.core)

    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.kotlin.logging)

    implementation(libs.log4j.api)

    runtimeOnly(libs.log4j.slf4j2)
    runtimeOnly(libs.jackson.databind)
    runtimeOnly(libs.jackson.dataformat.yaml)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
    testImplementation(libs.neo4j.harness)
}
