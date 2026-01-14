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
> Before Golem is started for the first time, the [neo4j-browser](https://neo4j.com/docs/browser-manual/current/) needs to be built from sources as a part of the `golem-xiv-web` module. It's a one-time action.

```shell
./gradlew installNeo4jBrowser
```

Install Playwright's Bundled Chromium

To enable web browsing with Playwright's bundled Chromium:

```shell
./gradlew installPlaywrightChromium
```

> [!NOTE]
> **Note for non Ubuntu Linux users**: Playwright officially supports Ubuntu 20.04, 22.04, and 24.04.
> On Arch Linux, the bundled Chromium may fail to launch due to missing shared libraries. In this case, Golem will automatically fall back to system Chromium.

To use system Chromium instead, install it via your package manager:

```shell
# Ubuntu/Debian
sudo apt install chromium-browser

# Arch Linux
sudo pacman -S chromium
```

If Chromium is installed in a non-standard location, you can specify it with the `--chromium-path` flag.

If Playwright cannot be initialized, Golem will automatically fall back to using jina.ai for web content fetching.


> [!NOTE]
> The order of service startup is important!

Open 4 separate terminals, in each of them run a different command:

First start neo4j graph db (memory as knowledge graph):

```shell
./gradlew runNeo4j
```

Start DDGS search service (web search):

```shell
./gradlew runDdgsSearch
```

This service automatically installs Python dependencies on first run.

> [!NOTE]
> On some Linux distributions, you may need to install `python3-venv` first: `sudo apt install python3-venv`

Then run the Golem server:

```shell
export ANTHROPIC_API_KEY=your_key
./gradlew run
```

**Optional:**

- To show the browser window when Golem uses Playwright (instead of running headless):
  ```shell
  export ANTHROPIC_API_KEY=your_key
  ./gradlew run --args="--show-browser"
  ```

- To specify a custom Chromium installation path:
  ```shell
  export ANTHROPIC_API_KEY=your_key
  ./gradlew run --args="--chromium-path=/path/to/chromium"
  ```

  >[!NOTE]
  >Using system chromium browser will probably work just fine, but technically it is discouraged in Playwright documentation. 

- Combine multiple options:
  ```shell
  export ANTHROPIC_API_KEY=your_key
  ./gradlew run --args="--show-browser --chromium-path=/usr/bin/google-chrome-stable"
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
