plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
}
