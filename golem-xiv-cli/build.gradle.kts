plugins {
    alias(libs.plugins.kotlin.multiplatform)
    //alias(libs.plugins.versions)
}

kotlin {

    jvm()

    macosArm64 {
        binaries {

        }
    }

    sourceSets {

        commonMain {
            dependencies {
                implementation(libs.ktor.client.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.xemantic.kotlin.test)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.java)
            }
        }

    }

}

tasks.withType<JavaExec>().configureEach {
    standardInput = System.`in`
}
