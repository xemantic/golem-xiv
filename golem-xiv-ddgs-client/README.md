# golem-xiv-ddgs-client

Ktor-based client for the DDGS (DuckDuckGo Search) API server.

## Overview

This module provides a Kotlin client for interacting with a DDGS API server instance. DDGS is a Python-based API server that provides programmatic access to DuckDuckGo search across various categories.

## Features

- **Text Search**: Web search with support for multiple backends (DuckDuckGo, Bing, Google)
- **Image Search**: Find images with metadata
- **Video Search**: Discover video content
- **News Search**: Query recent news articles
- **Book Search**: Search for books
- **Health Check**: Verify server availability

## Usage

```kotlin
import com.xemantic.ai.golem.ddgs.DdgsClient
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

// Create HTTP client with JSON serialization
val httpClient = HttpClient(Java) {
    install(ContentNegotiation) {
        json()
    }
}

// Create DDGS client
val client = DdgsClient(
    baseUrl = "http://localhost:8000",
    httpClient = httpClient
)

// Perform a text search
val results = client.searchText(
    query = "Kotlin programming language",
    maxResults = 10
)

results.forEach { result ->
    println("${result.title}: ${result.href}")
}
```

## Running Tests

The integration tests require Docker to be available, as they spin up a DDGS server container using Testcontainers.

### Prerequisites

- Docker installed and running
- Docker daemon accessible from the test environment

### Running Integration Tests

To run the integration tests, enable them with the system property:

```bash
./gradlew :golem-xiv-ddgs-client:test -Dddgs.tests.enabled=true
```

By default (without the system property), the tests are skipped to allow the build to succeed in environments without Docker.

## DDGS Server

The tests use the `deedy5/ddgs:latest` Docker image, which provides the DDGS API server. For more information about DDGS, see:

https://github.com/deedy5/ddgs

## API Reference

### DdgsClient

Main client class for interacting with the DDGS API.

#### Methods

- `searchText(query, region?, safesearch?, timelimit?, maxResults?, backend?): List<TextSearchResult>`
- `searchImages(query, region?, safesearch?, timelimit?, maxResults?): List<ImageSearchResult>`
- `searchVideos(query, region?, safesearch?, timelimit?, maxResults?): List<VideoSearchResult>`
- `searchNews(query, region?, safesearch?, timelimit?, maxResults?): List<NewsSearchResult>`
- `searchBooks(query, maxResults?): List<BookSearchResult>`
- `checkHealth(): HealthStatus`

## Platform Support

Currently JVM-only. Multiplatform support may be added in the future if needed.
