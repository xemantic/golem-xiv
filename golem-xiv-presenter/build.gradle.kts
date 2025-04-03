plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
}

kotlin {

    compilerOptions {
        //apiVersion = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())
        //languageVersion = kotlinTarget
        freeCompilerArgs.addAll(
//            "-Xmulti-dollar-interpolation",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi",
  //          "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
  //          "-opt-in=kotlin.time.ExperimentalTime"
        )
        extraWarnings = true
        progressiveMode = true
    }

    jvm {
    }

    js {
        browser()
        binaries.library()
    }

    sourceSets {

        commonMain {
            dependencies {
                implementation(project(":golem-xiv-api"))
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.websockets)
                implementation(libs.ktor.client.core)
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
