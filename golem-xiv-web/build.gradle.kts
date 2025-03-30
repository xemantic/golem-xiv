plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.plugin.power.assert)
    alias(libs.plugins.dokka)
    alias(libs.plugins.versions)
    `maven-publish`
    signing
}

kotlin {

//    compilerOptions {
//        apiVersion = kotlinTarget
//        languageVersion = kotlinTarget
//        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
//        extraWarnings = true
//        progressiveMode = true
//    }

    js {
        browser {

        }
        binaries.executable()
    }

    sourceSets {

        commonMain {
            dependencies {
                implementation(project(":golem-xiv-api"))
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.xemantic.kotlin.test)
            }
        }

        jsMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                //implementation("org.jetbrains.kotlinx-html-js:0.8.0")
            }
        }

    }

}

//powerAssert {
//    functions = listOf(
//        "com.xemantic.kotlin.test.assert",
//        "com.xemantic.kotlin.test.have"
//    )
//}
