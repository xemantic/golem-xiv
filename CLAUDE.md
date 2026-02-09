# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Golem XIV is a meta-cognitive recursive AI agent with memory, built as a Kotlin multiplatform project. It features:
- Knowledge Graph memory using Neo4j
- GolemScript - a custom Kotlin-based scripting language for agent reasoning
- Multiple LLM backends (Anthropic Claude, Alibaba Qwen/Dashscope)
- Real-time web interface via WebSockets

## Build Commands

```shell
# Build the entire project
./gradlew build

# Run tests for a specific module
./gradlew :golem-xiv-core:test
./gradlew :golem-xiv-neo4j:test

# Compile without tests
./gradlew :golem-xiv-core:compileKotlin
./gradlew :golem-xiv-neo4j:compileKotlin

# Clean local neo4j and disk storage
./gradlew cleanDevStorage
```

## Running the Application

Requires four terminals running in order:

1. **Neo4j database**: `./gradlew runNeo4j`
2. **DDGS search service**: `./gradlew runDdgsSearch` (automatically installs Python dependencies on first run). DDGS stands for "Dux Distributed Global Search" - a metasearch library aggregating results from multiple backends (Bing, Brave, DuckDuckGo, Google, Wikipedia, etc.). NOT "DuckDuckGo Search".
3. **Golem server**: `export ANTHROPIC_API_KEY=your_key && ./gradlew run`
4. **Web client**: `./gradlew jsBrowserDevelopmentRun --continuous`

### Golem Server Options

CLI arguments for the Golem server:

- `--show-browser`: Show the browser window when using webOpen (default is headless mode)
- `--chromium-path=/path/to/chromium`: Specify custom Chromium installation path

Examples:
```shell
# Show browser window
export ANTHROPIC_API_KEY=your_key && ./gradlew run --args="--show-browser"

# Use custom Chromium path
export ANTHROPIC_API_KEY=your_key && ./gradlew run --args="--chromium-path=/usr/bin/google-chrome-stable"

# Combine options
export ANTHROPIC_API_KEY=your_key && ./gradlew run --args="--show-browser --chromium-path=/opt/chromium/bin/chromium"
```

### First-time Setup

- Neo4j Browser: `./gradlew installNeo4jBrowser`
- DDGS service dependencies are installed automatically when you first run `./gradlew runDdgsSearch`
- (Optional) Playwright bundled Chromium: `./gradlew installPlaywrightChromium`
  - On Arch Linux, bundled Chromium may fail. Install system Chromium instead: `sudo pacman -S chromium`
  - Golem will automatically try bundled Chromium first, then fall back to system Chromium
  - If Playwright cannot be initialized, Golem will use jina.ai as fallback for web content

**Note**: On some Linux distributions, you may need to install `python3-venv` first: `sudo apt install python3-venv`

## Architecture

### Module Structure

- **golem-xiv-api** - Multiplatform API types shared between client/server (Cognition, Phenomenon, EpistemicAgent)
- **golem-xiv-api-backend** - Backend-only API extensions (CognitionRepository, Cognizer, GolemScriptApi)
- **golem-xiv-api-websocket** - WebSocket protocol types
- **golem-xiv-core** - Core cognitive processing engine (`GolemXiv` class, script execution)
- **golem-xiv-neo4j** - Neo4j Knowledge Graph memory implementation
- **golem-xiv-cognizer-anthropic** - Claude/Anthropic LLM integration via anthropic-sdk-kotlin
- **golem-xiv-server** - Ktor-based WebSocket server
- **golem-xiv-web** - Kotlin/JS browser client
- **golem-xiv-neo4j-starter** - Embedded Neo4j launcher for development

### Key Concepts

- **Cognition**: A reasoning session with an ID, tracking phenomenal expressions
- **Phenomenon**: Units of perception/expression (Text, Image, Document, Intent, Fulfillment)
- **EpistemicAgent**: An entity that can express phenomena (AI, Human, Computer)
- **Intent**: GolemScript code blocks that Golem wants to execute
- **Fulfillment**: Results from executing GolemScript intents

### Build Logic

The `build-logic` module provides `golem.convention` plugin applied to all modules, configuring:
- Kotlin/JVM target 21
- Power Assert for tests (`com.xemantic.kotlin.test.assert`, `com.xemantic.kotlin.test.have`)
- Progressive mode and extra warnings enabled

## Testing

Tests use JUnit Platform with `xemantic-kotlin-test` assertions (power-assert enabled). Neo4j tests use `neo4j-harness` for embedded database instances.

### Assertion Conventions

Use `com.xemantic.kotlin.test` instead of `kotlin.test` assertions. The only assertion function is `assert` with power-assert rendering complex boolean expressions on failure. Never use `assertEquals`, `assertContains`, `assertTrue`, `assertFalse`, `assertNotNull` from `kotlin.test`.

```kotlin
// equality - use assert with ==
assert(actual == expected)

// contains - use `in` operator
assert("Search Results" in result)
assert(element in list)

// negation
assert("error" !in output)
assert(!deleted)

// null checks
assert(value == null)

// scoped assertions on an object - use should/have
mockProvider should {
    have(lastQuery == "kotlin programming")
    have(lastPage == 1)
    have(lastPageSize == 10)
}

// type assertions - use should/be
content should {
    be<Text>()
    have(text.length > 0)
}

// string comparison with unified diff - use sameAs
actual sameAs expected

// exception assertions - assertFailsWith from kotlin.test is OK
val exception = assertFailsWith<IllegalArgumentException> {
    doSomething()
}
assert("expected message" in (exception.message ?: ""))
```

Imports:
```kotlin
import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.should
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.be
import com.xemantic.kotlin.test.sameAs
```

### Unit Tests

```shell
# Run a single test class
./gradlew :golem-xiv-core:test --tests "com.xemantic.ai.golem.core.script.GolemScriptExecutorTest"

# Run a single test method
./gradlew :golem-xiv-neo4j:test --tests "*Neo4jMemoryTest.should*"

# Run all tests in a module
./gradlew :golem-xiv-core:test
```

### Integration Tests

Integration tests are tagged with `@Tag("integration")` and require external services or network connectivity:

**Web Search Integration Tests** (require DDGS service):
```shell
# Start DDGS service first
./gradlew runDdgsSearch

# In another terminal, run integration tests
./gradlew :golem-xiv-core:test --tests "*Integration*"
```

**Web Browser Integration Tests** (require network connectivity):
```shell
# Run Playwright integration tests (opens real websites)
./gradlew :golem-xiv-playwright:test --tests "*Integration*"
```

**Run All Integration Tests**:
```shell
# Run all integration tests across modules
./gradlew test --tests "*Integration*"
```

Integration tests will be **skipped** (not failed) if required services are unavailable.

## IDE Setup

Optional: Install [Graph Database](https://plugins.jetbrains.com/plugin/20417-graph-database) IntelliJ plugin for Cypher syntax highlighting.