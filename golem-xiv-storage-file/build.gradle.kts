plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    api(project(":golem-xiv-api"))
    api(project(":golem-xiv-api-backend"))

    implementation(libs.kotlin.logging)
}
