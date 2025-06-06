# Golem XIV

A meta-cognitive recursive AI agent with memory.

## Starting Golem XIV

Open 3 separate terminals, in each of them run a different command:

> [!NOTE]
> The order is important!

First start neo4j graph db (memory as knowledge graph):

```shell
./gradlew runNeo4j
```

Then run the Golem server:
```shell
export ANTHROPIC_API_KEY=your_key
./gradlew run
```

Finally run the web client:
```shell
./gradlew jsBrowserDevelopmentRun --continuous
```

The last command will open your browser pointing to:

http://localhost:8080

communicating with Ktor-based Web server (with WebSockets) running on:

http://localhost:8081
