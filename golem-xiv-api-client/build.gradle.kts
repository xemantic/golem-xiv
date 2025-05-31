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
                implementation(project(":golem-xiv-api")) // TODO should not be needed with kotlin 2.2
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.websockets)
                implementation(libs.ktor.client.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.xemantic.kotlin.test)
            }
        }

    }

}
