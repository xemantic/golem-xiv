rootProject.name = "golem-xiv"

pluginManagement {
    includeBuild("build-logic")
}

// TODO can it be a series of paths?
include(
    ":golem-xiv-json",
    ":golem-xiv-api",
    ":golem-xiv-api-backend",
    ":golem-xiv-api-client",
    ":golem-xiv-logging",
    ":golem-xiv-core",
    ":golem-xiv-neo4j",
    ":golem-xiv-cognizer-anthropic",
//    ":golem-xiv-cognizer-dashscope",
    ":golem-xiv-playwright",
    ":golem-xiv-server",
    ":golem-xiv-presenter",
    ":golem-xiv-web",
    ":golem-xiv-cli",
    ":golem-xiv-neo4j-starter",
    ":golem-xiv-kotlin-metadata",
)
