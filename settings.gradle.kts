rootProject.name = "golem-xiv"

pluginManagement {
//    includeBuild("build-logic")
//    repositories {
//        mavenCentral()
//        gradlePluginPortal()
//    }
}

include(":golem-xiv-api")
include(":golem-xiv-presenter")
include(":golem-xiv-server")
include(":golem-xiv-web")
include(":golem-xiv-cli")
include(":golem-xiv-neo4j")
