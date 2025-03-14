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
//
    jvm {
        // set up according to https://jakewharton.com/gradle-toolchains-are-rarely-a-good-idea/
//        compilerOptions {
//            apiVersion = kotlinTarget
//            languageVersion = kotlinTarget
//            jvmTarget = JvmTarget.fromTarget(javaTarget)
//            freeCompilerArgs.add("-Xjdk-release=$javaTarget")
//            progressiveMode = true
//        }
    }

    js {
        browser()
        // TODO remove for a non-library project
        binaries.library()
    }

//    wasmJs {
//        browser()
//        nodejs()
//        //d8()
//        // TODO remove for a non-library project
//        binaries.library()
//    }
//
//    wasmWasi {
//        nodejs()
//        // TODO remove for a non-library project
//        binaries.library()
//    }
//
//    // native, see https://kotlinlang.org/docs/native-target-support.html
//    // tier 1
//    macosX64()
//    macosArm64()
//    iosSimulatorArm64()
//    iosX64()
//    iosArm64()
//
//    // tier 2
//    linuxX64()
//    linuxArm64()
//    watchosSimulatorArm64()
//    watchosX64()
//    watchosArm32()
//    watchosArm64()
//    tvosSimulatorArm64()
//    tvosX64()
//    tvosArm64()
//
//    // tier 3
//    androidNativeArm32()
//    androidNativeArm64()
//    androidNativeX86()
//    androidNativeX64()
//    mingwX64()
//    watchosDeviceArm64()
//
//    @OptIn(ExperimentalSwiftExportDsl::class)
//    swiftExport {}

    sourceSets {

        commonMain {
            dependencies {
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

//powerAssert {
//    functions = listOf(
//        "com.xemantic.kotlin.test.assert",
//        "com.xemantic.kotlin.test.have"
//    )
//}
