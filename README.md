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

## Authentication

Golem XIV supports optional HTTP Basic Authentication. When the `httpAuth` section is absent from the configuration, all routes are open — this is the default for local development.

### Local development

Authentication is disabled by default in `application.yaml`. No credentials are required when running locally.

### Enabling authentication for deployment

Authentication is enabled when `application-deployment.yaml` is used. The password is stored as a bcrypt hash (cost factor 12). To generate one:

```shell
# Using htpasswd (available on Linux/macOS via apache2-utils / httpd-tools)
htpasswd -bnBC 12 "" "your_password" | tr -d ':\n'

# Or using Python 3
python3 -c "import bcrypt; print(bcrypt.hashpw(b'your_password', bcrypt.gensalt(12)).decode())"
```

Then set the following environment variables before deploying:

```shell
export GOLEM_AUTH_USERNAME=your_username
export GOLEM_AUTH_BCRYPT_HASH='$2b$12$...'   # single-quote to avoid $ expansion
```

These are picked up by `application-deployment.yaml`.

> [!WARNING]
> HTTP Basic Auth sends credentials as base64 — effectively plaintext. Always deploy behind a TLS-terminating reverse proxy (nginx, Caddy, Traefik, etc.).

## Developing Golem

### Optional Cypher syntax highlighting
The [Graph Database](https://plugins.jetbrains.com/plugin/20417-graph-database) IntelliJ plugin adds syntax highlighting to Cypher queries

