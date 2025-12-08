plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("golem.convention")
}

dependencies {
    api(project(":golem-xiv-api-backend"))
    implementation(project(":golem-xiv-logging"))

    api(libs.neo4j.java.driver)
    api(libs.xemantic.neo4j.kotlin.driver)

    implementation(libs.neo4j.migrations)
    implementation(libs.logback.classic)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
    testImplementation(libs.neo4j.harness) {
        // we are running tests involving both - the driver and neo4j instance itself
        // for this reason we need to keep only one logging bindings provider (logback)
        exclude(group = "org.neo4j",  module = "neo4j-slf4j-provider")
    }
}
