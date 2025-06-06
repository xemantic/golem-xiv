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
                api(libs.kotlinx.serialization.json)
                api(libs.ktor.client.core)

                implementation(libs.kotlin.logging)
            }
        }

    }

}
