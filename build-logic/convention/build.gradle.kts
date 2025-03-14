import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.xemantic.ai.golem.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
//    compileOnly(libs.plugins.kotlin.multiplatform)
//    compileOnly(libs.plugins.kotlin.plugin.power.assert)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("jvmLibrary") {
            id = libs.plugins.golem.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
    plugins {
        register("golemModule") {
            id = libs.plugins.golem.module.get().pluginId
            implementationClass = "GolemModulePlugin"
        }
    }
}
