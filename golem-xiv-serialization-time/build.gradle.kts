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
                api(libs.kotlinx.serialization.core)
            }
        }

    }

}
