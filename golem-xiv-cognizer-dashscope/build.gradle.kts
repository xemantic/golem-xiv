plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    api(project(":golem-xiv-api"))
    api(project(":golem-xiv-api-backend"))

    implementation(libs.dashscope) {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }

    implementation(libs.kotlinx.coroutines.rx2)
}
