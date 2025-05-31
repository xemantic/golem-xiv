plugins {
    alias(libs.plugins.kotlin.jvm)
    id("golem.convention")
}

dependencies {
    implementation(project(":golem-xiv-api-backend"))

    api(libs.playwright)

    // TODO flexmark is suboptimal
    implementation("com.vladsch.flexmark:flexmark:0.64.8")
    implementation("com.vladsch.flexmark:flexmark-html2md-converter:0.64.8")

    implementation(libs.kotlin.logging)

    implementation(libs.log4j.api)

    runtimeOnly(libs.log4j.slf4j2)
    runtimeOnly(libs.jackson.databind)
    runtimeOnly(libs.jackson.dataformat.yaml)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.xemantic.kotlin.test)
    testImplementation(libs.neo4j.harness)
}
