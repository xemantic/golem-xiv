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

Requires three terminals running in order:

1. **Neo4j database**: `./gradlew runNeo4j`
2. **Golem server**: `export ANTHROPIC_API_KEY=your_key && ./gradlew run`
3. **Web client**: `./gradlew jsBrowserDevelopmentRun --continuous`

First-time setup requires: `./gradlew installNeo4jBrowser`

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

Tests use JUnit Platform with `xemantic-kotlin-test` assertions. Neo4j tests use `neo4j-harness` for embedded database instances.

```shell
# Run a single test class
./gradlew :golem-xiv-core:test --tests "com.xemantic.ai.golem.core.script.GolemScriptExecutorTest"

# Run a single test method
./gradlew :golem-xiv-neo4j:test --tests "*Neo4jMemoryTest.should*"
```

## IDE Setup

Optional: Install [Graph Database](https://plugins.jetbrains.com/plugin/20417-graph-database) IntelliJ plugin for Cypher syntax highlighting.