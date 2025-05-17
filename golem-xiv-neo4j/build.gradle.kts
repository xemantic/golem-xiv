plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    compilerOptions {
        //apiVersion = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())
        //languageVersion = kotlinTarget
        freeCompilerArgs.addAll(
            "-Xmulti-dollar-interpolation"
        )
        extraWarnings = true
        progressiveMode = true
    }
}

dependencies {
    implementation(libs.neo4j)
}

tasks.register<JavaExec>("startNeo4j") {
    description = "Start Neo4j embedded database with optimized JVM settings"
    group = "neo4j"

    standardInput = System.`in`
    standardOutput = System.out
    errorOutput = System.err

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.xemantic.ai.golem.neo4j.GolemNeo4JKt")

    jvmArgs("-Xms512m", "-Xmx1g")

    // Make task interruptible
    doFirst {
        println("Starting Neo4j (DEV MODE)")
        println("Press Ctrl+C to stop")
    }

}
