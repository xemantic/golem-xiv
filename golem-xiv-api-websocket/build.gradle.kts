plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("golem.convention")
}

kotlin {

    jvm()

    js {
        browser()
        binaries.library()
    }

    sourceSets {

        commonMain {
            dependencies {
                api(project(":golem-xiv-api"))
                api(libs.kotlinx.coroutines.core)
                api(libs.ktor.websockets)

                implementation(libs.kotlin.logging)
                implementation(libs.kotlinx.serialization.json)
            }
        }

    }

}
