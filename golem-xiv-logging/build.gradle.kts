plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    api(libs.kotlin.logging)
    implementation(libs.jul.to.slf4j)
}
