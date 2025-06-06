plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    implementation(project(":golem-xiv-api"))
    implementation(project(":golem-xiv-api-backend"))

    implementation(libs.kotlin.logging)

    implementation(libs.log4j.api)

    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.jvm.host)

    runtimeOnly(libs.log4j.slf4j2)
    runtimeOnly(libs.jackson.databind)
    runtimeOnly(libs.jackson.dataformat.yaml)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
    testImplementation(libs.neo4j.harness)
}
