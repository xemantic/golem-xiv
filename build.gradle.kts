plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
//    alias(libs.plugins.kotlin.plugin.serialization)
//    alias(libs.plugins.kotlin.plugin.power.assert)
////    alias(libs.plugins.kotlinx.binary.compatibility.validator)
//    alias(libs.plugins.dokka)
    alias(libs.plugins.versions)
    `maven-publish`
    signing
}

// TODO change the group
group = "com.xemantic.ai.golem"

//// TODO fill up the details
//xemantic {
//    description = "A template repository for Xemantic's Kotlin multiplatform projects"
//    inceptionYear = 2025
//    license = License.APACHE
//    developer(
//        id = "morisil",
//        name = "Kazik Pogoda",
//        email = "morisil@xemantic.com"
//    )
//}


//val javaTarget = libs.versions.javaTarget.get()
//val kotlinTarget = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())


allprojects {
    repositories {
        mavenCentral()
    }

//    tasks.withType<AbstractArchiveTask> {
//        // Only set if not already set
//        if (!archiveBaseName.isPresent) {
//            archiveBaseName.set(project.name)
//        }
//        archiveVersion.set(project.version.toString())
//    }
//    apply(plugin = "maven-publish")
}

tasks.register<Delete>("cleanDevStorage") {
    group = "golem"
    description = "Cleans local dev neo4j and disk storage"
    delete(fileTree("var/neo4j/data"))
    delete(fileTree("var/neo4j/logs"))
    delete(fileTree("var/cognitions"))
}
