# Golem XIV - DDGS Search Service

A lightweight HTTP service providing web search capabilities for Golem XIV using the DDGS (Dux Distributed Global Search) metasearch library.

## Overview

This module wraps the [DDGS library](https://github.com/deedy5/ddgs) in a FastAPI HTTP service, allowing Golem XIV to perform web searches without relying on expensive commercial search APIs.

## Features

- **Multiple search backends**: Automatically selects from bing, brave, duckduckgo, google, wikipedia, and more
- **Web search**: Text-based search with customizable parameters
- **Image search**: Find images with size, color, and type filters
- **News search**: Search recent news articles
- **RESTful API**: Simple HTTP endpoints for easy integration
- **OpenAPI docs**: Interactive API documentation at `/docs`

## Requirements

- Python >= 3.10
- python3-venv (on some Linux distributions, install with: `sudo apt install python3-venv`)

## Installation

The service uses a Python virtual environment that is automatically created and configured.

Install Python dependencies (automatically creates venv if needed):

```bash
./gradlew installDdgsDeps
```

This will:
1. Create a virtual environment in `src/main/python/venv/`
2. Install all required dependencies (fastapi, uvicorn, ddgs) in the venv

## Running the Service

Start the DDGS search service:

```bash
./gradlew runDdgsSearch
```

This will:
1. Ensure the venv exists and dependencies are installed
2. Start the service using the venv's Python
3. Service will be available at `http://localhost:8001`

## API Endpoints

### Health Check

```bash
GET http://localhost:8001/health
```

### Web Search

```bash
GET http://localhost:8001/search?query=xemantic&max_results=10
```

**Parameters:**
- `query` (required): Search query
- `region` (optional, default: "us-en"): Search region (e.g., "uk-en", "ru-ru")
- `safesearch` (optional, default: "moderate"): Safety level ("on", "moderate", "off")
- `timelimit` (optional): Time filter ("d", "w", "m", "y")
- `max_results` (optional, default: 10): Maximum number of results (1-100)
- `page` (optional, default: 1): Page number
- `backend` (optional, default: "auto"): Search backend ("bing", "brave", "duckduckgo", "google", etc.)

**Response:**
```json
[
  {
    "title": "xemantic",
    "href": "https://xemantic.com/",
    "body": "My name is Kazik Pogoda and xemantic logo is a label I have been using..."
  }
]
```

### Image Search

```bash
GET http://localhost:8001/images?query=butterfly&max_results=5
```

**Additional Parameters:**
- `size`: "Small", "Medium", "Large", "Wallpaper"
- `color`: "Monochrome", "Red", "Orange", etc.
- `type_image`: "photo", "clipart", "gif", "transparent", "line"
- `layout`: "Square", "Tall", "Wide"

### News Search

```bash
GET http://localhost:8001/news?query=AI&timelimit=w&max_results=5
```

**Parameters:** Similar to web search, with news-specific backends

## Interactive Documentation

Once the service is running, visit:

- API Docs: http://localhost:8001/docs
- Alternative Docs: http://localhost:8001/redoc

## Usage from GolemScript

Once integrated with Golem XIV, you can use the web search functionality:

```kotlin
val results = web.search("xemantic")
```

Or with specific parameters:

```kotlin
val results = web.search(
    query = "AI agents",
    provider = "ddgs",  // Use DDGS service
    page = 1,
    pageSize = 10,
    region = "us-en",
    safesearch = "moderate",
    timelimit = "m"  // Last month
)
```

## Supported Search Backends

| Function | Available Backends |
|----------|-------------------|
| text() | bing, brave, duckduckgo, google, grokipedia, mojeek, yandex, yahoo, wikipedia |
| images() | duckduckgo |
| news() | bing, duckduckgo, yahoo |

## Architecture

```
GolemScript (Kotlin)
    ↓
Web Interface
    ↓
HTTP Client (Ktor)
    ↓
DDGS Service (Python/FastAPI) ← localhost:8001
    ↓
DDGS Library
    ↓
Search Engines (Bing, DuckDuckGo, Google, etc.)
```

## License

This module is part of Golem XIV and is licensed under the GNU Affero General Public License v3.0 or later.

## Credits

Built on top of the excellent [DDGS library](https://github.com/deedy5/ddgs) by deedy5.
