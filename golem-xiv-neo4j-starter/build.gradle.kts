plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    implementation(libs.neo4j)
}

tasks.register<JavaExec>("runNeo4j") {
    description = "Start Neo4j embedded database with optimized JVM settings"
    group = "golem"

    standardInput = System.`in`
    standardOutput = System.out
    errorOutput = System.err

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.xemantic.ai.golem.neo4j.starter.GolemNeo4jKt")

    jvmArgs("-Xms512m", "-Xmx1g")

    // Make task interruptible
    doFirst {
        println("Starting Neo4j (DEV MODE)")
        println("Press Ctrl+C to stop")
    }
}
