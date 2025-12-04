plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    api(project(":golem-xiv-api-backend"))
    api(libs.neo4j.java.driver)

    implementation(libs.kotlin.logging)
    implementation(libs.xemantic.neo4j.kotlin.driver)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
    testImplementation(libs.neo4j.harness)
}
