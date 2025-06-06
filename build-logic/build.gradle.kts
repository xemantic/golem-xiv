plugins {
    `kotlin-dsl`
}

val javaTarget = libs.versions.javaTarget.get()

java {
    val version = JavaVersion.toVersion(javaTarget)
    sourceCompatibility = version
    targetCompatibility = version
}

tasks.compileJava {
    options.release = javaTarget.toInt()
}

kotlin {
    compilerOptions {
        val specifiedJvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaTarget)
        jvmTarget = specifiedJvmTarget
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.power.assert)
}

gradlePlugin {
    plugins {
        register("GolemConventionPlugin") {
            id = "golem.convention"
            implementationClass = "con.xemantic.ai.golem.buildlogic.GolemConventionPlugin"
        }
    }
}
