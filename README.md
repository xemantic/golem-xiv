# Golem XIV

A meta-cognitive recursive AI agent with memory.

## Starting Golem XIV

> [!NOTE]
> Before Golem is started for the first time, the [neo4j-browser](https://neo4j.com/docs/browser-manual/current/) needs to be built from sources as a part of the `golem-xiv-web` module. It's a one-time action.

```shell
./gradlew installNeo4jBrowser
```

> [!NOTE]
> The order of service startup is important!

Open 3 separate terminals, in each of them run a different command:

First start neo4j graph db (memory as knowledge graph):

```shell
./gradlew runNeo4j
```

Then run the Golem server:
```shell
export ANTHROPIC_API_KEY=your_key
./gradlew run
```

finally, run the web client:

```shell
./gradlew jsBrowserDevelopmentRun --continuous
```

The last command will open your browser pointing to:

http://localhost:8080

communicating with Ktor-based Web server (with WebSockets) running on:

http://localhost:8081

## Developing Golem

### Optional Cypher syntax highlighting
The [Graph Database](https://plugins.jetbrains.com/plugin/20417-graph-database) IntelliJ plugin adds syntax highlighting to Cypher queries

