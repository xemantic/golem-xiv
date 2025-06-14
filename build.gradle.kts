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

tasks.register<Exec>("cleanDevStorage") {
    group = "other"
    description = "Cleans local dev neo4j and disk storage"
    val varDir = File("var")
    val neo4jDir = File(varDir, "neo4j")
    File(neo4jDir, "data").clearDirectory()
    File(neo4jDir, "logs").clearDirectory()
    File(varDir, "cognitions").clearDirectory()
}

fun File.clearDirectory() {

    if (!exists() || !isDirectory) return

    walkTopDown()
        .filter { it != this@clearDirectory } // Keep the root directory
        .sortedByDescending { it.path.length } // Delete deepest files first
        .forEach { it.delete() }
}
