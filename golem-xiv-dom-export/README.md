# golem-xiv-dom-export

A Kotlin/JS module that compiles to a single JavaScript function designed for injection
into Playwright or any CDP (Chrome DevTools Protocol) session. When invoked on a
`Document`, it produces a stream of semantic events representing the DOM tree structure,
with special support for identifying and referencing actionable elements.

## Purpose

Typical browser automation relies on CSS selectors or XPath queries to locate elements.
This approach is fragile and requires the caller to understand the page structure in
advance. Instead, this module exports the DOM tree as a sequence of semantic events that
an AI agent can interpret directly, while simultaneously building a mapping of actionable
elements that can be targeted for interaction without constructing selectors.

## How It Works

### 1. DOM Tree Export as Semantic Events

The exported `exportSemanticEvents(document)` function traverses the entire DOM tree and emits
a newline-delimited sequence of JSON events based on the
[markanywhere](https://github.com/xemantic/markanywhere) semantic event model:

- **`MarkEvent`** -- opening of an HTML element (tag name, attributes)
- **`UnmarkEvent`** -- closing of an HTML element
- **`TextEvent`** -- text content

This representation preserves the full document structure in a flat, streamable format
that is straightforward for an LLM to process.

### 2. Actionable Element Detection and Registration

Elements that can be interacted with (clicked, toggled, submitted) are detected
heuristically. Each actionable element receives a `golemId` attribute directly in its
`MarkEvent`, and is registered in a global `Map<string, Element>` at
`globalThis.__golemElements`. The following elements are considered actionable:

| Element | Condition |
|---|---|
| `<a>` | Only when **without** `href` (links with `href` are self-describing) |
| `<button>` | Always |
| `<select>` | Always |
| `<summary>` | Always |
| `<input>` | When type is `button`, `submit`, `reset`, `checkbox`, or `radio` |
| Any element | When it has an `onclick` attribute, `role="button"`, or computed `cursor: pointer` |

The `golemId` attribute appears as a regular attribute in the event stream, giving the
AI agent a stable reference to the element. IDs are short and sequential (`0`, `1`,
..., `N`), generated fresh on each export invocation. They are ephemeral -- valid only
until the next page navigation or re-export.

This allows subsequent automation commands (e.g. Playwright `page.evaluate`) to resolve
an element directly by its `golemId` without needing any CSS/XPath selector.

A convenience function `getElementByGolemId(golemId)` is also exported, providing direct
element lookup:

```js
// After injection, the agent can reference elements by golemId:
const el = getElementByGolemId("3");
el.click();
```

## Build Output

The module compiles to a single minified JavaScript file:

```
build/dist/js/productionExecutable/golem-xiv-dom-export.js
```

This file can be injected into any browser context via:
- Playwright's `page.addScriptTag()` or `page.evaluate()`
- CDP's `Runtime.evaluate`
- Any other mechanism that executes JS in a page context

## Usage

```js
// Inject and call
const events = exportSemanticEvents(document);
// events is a newline-delimited string of JSON objects
```

## Development

```shell
# Build the production JS bundle
./gradlew :golem-xiv-dom-export:build

# Run tests
./gradlew :golem-xiv-dom-export:jsTest
```