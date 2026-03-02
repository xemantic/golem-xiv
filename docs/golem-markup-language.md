# Golem Markup Language

The stream of input and output text building the context window used during LLM [inference](inference.md) is annotated with additional markup with `golem` namespace, consisting out of the following elements:

- cognition
- actant
- enunciation
- phenomenon

## `<golem:cognition>`

The `<golem:cognition>` tag appears only at the beginning of the cognitive process and does not contain other elements.

Attributes:

- `id`: the cognition sequence id unique within the system

## `<golem:enunciation>`

Attributes:

- `id`: the enunciation sequence id unique within the cognition
- `initiatedAt`: time
- `origin`: `invoked` | `dispatched` | `scheduled` | `triggered`
   - `invoked` attributes:
      - `parentId`
   - `dispatched` attributes:
      - `parentId`
   - `scheduled` attributes:
     - `cron`
   - `triggered` attributes:
     - TBD

### `<golem:actant>`

## Examples

### Scenario 1: Fibonacci number calculation

Step 1: A logged in and identified user inputs text:

```
What's Fibonacci number 42?
```

Step 2: Golem will translate it to the following content sent to the [cognizer](cognizer.md):

```markdown
<golem:cognition id="1234" initiatedAt="2026-02-17T09:09:53+00:00"/>
<golem:enunciation id="1" initiatedAt="2026-02-17T09:09:54+00:00">
<golem:actant id="12" identity="human"/>
</golem:enunciation>
<golem:phenomenon id="1" type="artifact" form="text">
What's Fibonacci number 42?
</golem:phenomenon>
```

> [!NOTE]
> Unique ids are assigned by Neo4j

The cognizer could reason:

```markdown
I will help you to calculate the 42nd Fibonacci number.
<golem:phenomenon type="intent" form="code" goal="Calculate the 42nd Fibonacci number">
fun fibonacci(n: Int): Long {
    if (n <= 1) return n.toLong()
    
    var prev = 0L
    var curr = 1L
    
    for (i in 2..n) {
        val next = prev + curr
        prev = curr
        curr = next
    }
    
    return curr
}

val result = fibonacci(42)
output.text {
    +"The 42nd Fibonacci number is: $result"
}
</golem:phenomenon>
```

Which then Golem interprets as:

```markdown
<golem:enunciation id="2" initiatedAt="2026-02-17T09:09:56+00:00"/>
<golem:actant id="1" identity="self" details="Claude Opus 4.6 (extended thinking)"/>
<golem:phenomenon id="2" type="artifact" form="text">
I will help you to calculate the 42nd Fibonacci number.
</golem:phenomenon>
<golem:phenomenon id="3" type="intent" form="code" goal="Calculate the 42nd Fibonacci number">
fun fibonacci(n: Int): Long {
    if (n <= 1) return n.toLong()
    
    var prev = 0L
    var curr = 1L
    
    for (i in 2..n) {
        val next = prev + curr
        prev = curr
        curr = next
    }
    
    return curr
}

val result = fibonacci(42)
output.text {
    +"The 42nd Fibonacci number is: $result"
}
</golem:phenomenon>
```

Response from the ephemeral computer resource:

```markdown
<golem:enunciation id="3" initiatedAt="2026-02-17T09:09:57+00:00"/>
<golem:actant id="10123" identity="computer" details="Cloud PC (specs)"/>
<golem:phenomenon id="4" type="fulfilment" form="text" fulfills="3">
The 42nd Fibonacci number is: 267914296
</golem:phenomenon>
```

Cognizer's raw answer:

```markdown
The 42nd Fibonacci number is **267,914,296**.
```

If cognition is progressed further it will become:

```markdown
<golem:enunciation id="4" initiatedAt=""/>
<golem:actant id="1" identity="self" details="Claude Opus 4.6 (extended thinking)"/>
<golem:phenomenon type="text">
The final answer is ....
</golem:phenomenon>
```

### Scenario 2: What's happening in the world today?

A logged in and identified user inputs:

```
What's happening in the world today?
```

Will result in the following content sent to the [cognizer](cognizer.md) (LLM):

```markdown
<golem:cognition id="1235" initiatedAt="2026-02-17T10:09:53+00:00"/>
<golem:enunciation id="1" initiatedAt="2026-02-17T10:09:54+00:00">
<golem:actant id="12" identity="human"/>
</golem:enunciation>
<golem:phenomenon id="1" type="artifact" form="text">
What's happening in the world today?
</golem:phenomenon>
```

The cognizer could reason:

```markdown
I will check today's news for you.
<golem:phenomenon type="intent" form="code" goal="Fetch BBC world news">
output.url {
    +web.fetch("https://www.bbc.com/news/world")
}
</golem:phenomenon>
```

Which eventually becomes:

```markdown
<golem:enunciation id="2" initiatedAt="2026-02-17T10:09:56+00:00"/>
<golem:actant id="1" identity="self" details="Claude Opus 4.6 (extended thinking)"/>
<golem:phenomenon id="2" type="artifact" form="text">
I will check today's news for you.
</golem:phenomenon>
<golem:phenomenon id="3" type="intent" form="code" goal="Fetch BBC world news">
output.url {
    +web.fetch("https://www.bbc.com/news/world")
}
</golem:phenomenon>
```

Response from the computer:

```markdown
<golem:enunciation id="3" initiatedAt="2026-02-17T10:09:57+00:00"/>
<golem:actant id="10123" identity="computer" details="Cloud PC (specs)"/>
<golem:phenomenon id="4" type="fulfilment" fulfills="3" form="markdown" url="https://www.bbc.com/news/world" status="200">
---
title: World | Latest News & Updates | BBC News
---
<header>
[](https://www.bbc.com/)

[Watch Live](https://www.bbc.com/watch-live-news/)
</header>
<nav>
* [Home](https://www.bbc.com/)
* [News](https://www.bbc.com/news)
* [Sport](https://www.bbc.com/sport)
* [Business](https://www.bbc.com/business)
* [Technology](https://www.bbc.com/technology)
* [Health](https://www.bbc.com/health)
* [Culture](https://www.bbc.com/culture)
* [Arts](https://www.bbc.com/arts)
* [Travel](https://www.bbc.com/travel)
* [Earth](https://www.bbc.com/future-planet)
* [Audio](https://www.bbc.com/audio)
* [Video](https://www.bbc.com/video)
* [Live](https://www.bbc.com/live)
* [Documentaries](https://www.bbc.com/video/docs)
</nav>
<section>
<a href="/news/articles/czx47k34yqxo">
[![Image 1: Hillary Clinton in BBC interview](https://ichef.bbci.co.uk/news/480/cpsprodpb/184b/live/2519e010-0bcd-11f1-b7e1-afb6d0884c18.jpg.webp)](https://www.bbc.com/news/articles/czx47k34yqxo)

## Hillary Clinton accuses Trump administration of Epstein files 'cover-up' in BBC interview

"Get the files out. They are slow-walking it," the former US secretary of state says. The White House says it has done "more for the victims than Democrats ever did".

</a>
...
</golem:phenomenon>
```


---
Work in progress:


---

```markdown
<golem:cognition id="1"/>
<golem:enunciation id="1">
<golem:actant id="0" identity="self" details="Claude Opus 4.6 (extended thinking)"/>
<golem:goal>Create a website</golem:goal>
<golem:rationale>
X needs to be presented online
</golem:rationale>
I will create files to update the website.
<golem:intent to="create-file" path="/home/website/index.html">
<html>
  <body>
  </body>
</html>
</golem:intent>
<golem:match replace-all="true" regex="false">John Smith</golem:match>
<golem:replacement>
<div class="person">
John Smith
</div>
</golem:replacement>
</golem:inent>
<golem:intent to="execute-script">
files.list("/home/website")
files.create("""

"""
</golem:intent>
</golem:enunciation>
```

```markdown
<golem:enunciation id="1">
<golem:actant id="0" identity="computer" details="Users's computer"/>
<golem:fulfillment of="file-creation" path="/home/website/index.html"/>
<golem:impediment of="replace-infile" path="/home/website/people.html">
The file does not exist
</golem:impediment>
<golem:fulfillment of="execute-script">

</golem:fulfillment>
<golem:phenomenon type=""
<golem:result type="file-creation">
</golem:result>
</golem:fulfillment>

X needs to be presented online
</golem:rationale>
<golem:create-file path="/home/website/index.html">
<html>
  <body>
  </body>
</html>
</golem:create-file>
<golem:replace path="/home/website/index.html">
<golem:match replace-all="true" regex="false">John Smith</golem:match>
<golem:replacement>
</golem:replacement>
<div class="person">
John Smith
</div>
</golem:replace>
</golem:enunciation>
```

## actant

In reference to Bruno Latour's philosophy:

Example:

```markdown
<golem:actant id="0" category="self" details="Claude Opus 4.6 (extended thinking)">
</golem:actant>
```

- self

## intent

### create-file

### replace-in-file

### script

```markdown
<golem:intent>
<golem:create-file path="/home/foo/bar.txt">
Lorem ipsum
</golem:create-file>
<golem:create-file path="/home/foo/buzz.txt">
Buzz buzz buzz
</golem:create-file>
<golem:replace-in-file>
</golem:replace-in-file>
</golem:intent>
```
