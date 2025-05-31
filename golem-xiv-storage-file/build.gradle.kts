plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":golem-xiv-api"))
    api(project(":golem-xiv-api-backend"))

    implementation(libs.kotlin.logging)
}
