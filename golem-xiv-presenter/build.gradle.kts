plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
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
                api(project(":golem-xiv-api-client"))
                api(libs.kotlinx.coroutines.core)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlin.logging)
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
