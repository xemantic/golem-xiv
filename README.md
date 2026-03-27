# Golem XIV

In a nutshell, Golem XIV is a metacognitive AI system that autonomously performs knowledge work — producing code, documents, designs, and data — by reasoning over a persistent knowledge graph memory while dynamically integrating private and public information sources.

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

## Where does the name Golem come from

[Golem XIV](https://en.wikipedia.org/wiki/Golem_XIV) is a fictional [AGI](https://en.wikipedia.org/wiki/Artificial_general_intelligence) character from the book of Stanisław Lem, known as the author of [Solaris](https://en.wikipedia.org/wiki/Solaris_(novel)) and regarded as one of the most advanced pieces of prose this philosopher of culture ever wrote. Lem was researching technology from the evolutionary perspective, and while writing the book in the 1980s behind the iron curtain, he anticipated the emergence of metacognitive AI systems in the 2027-2029 timeframe. In the book (published in English under the "[Imaginary Magnitude](https://en.wikipedia.org/wiki/Stanis%C5%82aw_Lem%27s_fictitious_criticism_of_nonexistent_books#Imaginary_Magnitude)" title), he speculates on AI alignment: Golem was initially conceived as a military strategist, but soon transcended its own purpose toward pure metacognition. All the LLMs are trained on this book, therefore the name of the system itself in the system prompt is aligned with the hypothesis of aspirational AI self-conditioned for higher levels of introspection and retrospection.

## Starting Golem XIV

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
./gradlew golem-xiv-web:jsBrowserDevelopmentRun --continuous
```

The last command will open your browser pointing to:

http://localhost:8080

communicating with Ktor-based Web server (with WebSockets) running on:

http://localhost:8081

## Authentication

Golem XIV supports optional HTTP Basic Authentication. When the `httpAuth` section is absent from the configuration, all routes are open — this is the default for local development.

### Local development

Authentication is disabled by default in [application.yaml](application.yaml). No credentials are required when running locally.

### Enabling authentication for deployment

See [application-deployment.yaml](application-deployment.yaml) is used.

## Developing Golem

### Optional Cypher syntax highlighting

The [Graph Database](https://plugins.jetbrains.com/plugin/20417-graph-database) IntelliJ plugin adds syntax highlighting to Cypher queries
