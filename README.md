# Golem XIV

A meta-cognitive recursive AI agent with memory.

## Features

* **Cognition**: focus on rather reasoning than communication
* **Metacognition**: ability to think about own thinking from the past
* **Unlimited memory**: represented as Knowledge Graph
* **Private context**: the memory is private to an organization
* **Mass parallelism**: several automatic cognitions at the time
* **Self-modification**: Golem is using own programming language
* **Chain-of-code**: it is thinking in code more than in English
* **Unix-omnipotence** full control over any machine
* **Auto-science**: creating hypothesis and verifying them
* **LLM-independence**: The cognition can be performed by any reasoning LLM: OpenAI (GPT), Anthropic (Claude), Google (Gemini), Mistral, Deepseek, Alibaba (Qwen) or any other reasoning model emerging in the future

## Starting Golem XIV

> [!NOTE]
> Before Golem is started for the first time, the [neo4j-browser](https://neo4j.com/docs/browser-manual/current/) needs to be installed as a part of the `golem-xiv-web` module. It's a one-time action.

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

