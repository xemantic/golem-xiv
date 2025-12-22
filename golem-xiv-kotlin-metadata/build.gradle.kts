plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    implementation(project(":golem-xiv-api-backend"))
    implementation(libs.kotlin.metadata.jvm)
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.logging)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.xemantic.kotlin.test)
    testRuntimeOnly(libs.logback.classic)
}
