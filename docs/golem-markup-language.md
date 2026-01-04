# Golem Markup Language

This document specifies the markup elements used by LLMs to communicate structured intentions, actions, and metacognitive states within the Golem XIV cognitive architecture. These elements bridge natural language reasoning and executable code, providing both machine-parseable structure and human-auditable transparency.

## Design Principles

The markup language follows several core principles:

1. **Intentionality over implementation** — Elements capture the *directedness* of cognitive acts, not just their mechanical execution.
2. **Auditability by design** — Every action carries its reasoning, enabling both self-review by Golem and inspection by human operators.
3. **Inference-friendly ordering** — Attribute sequences follow natural cognitive flow, producing cleaner LLM output through token-by-token commitment.
4. **Layered disclosure** — Information is structured for different consumption modes: scanning, investigating, and auditing.

---

## `<golem:intent>`

Declares a bounded cognitive act that impacts the environment through executable code. An intent represents the fundamental unit of Golem's agency — the coupling of *what is to be achieved* with *why it should be achieved* and *how it will be achieved*.

### Structure

```xml
<golem:intent goal="..." rationale="...">
  // Kotlin Script code
</golem:intent>
```

### Attributes

| Attribute | Required | Description |
|-----------|----------|-------------|
| `goal` | yes | Declarative statement of the intended outcome. Answers: *what is being done?* |
| `rationale` | no | Justification for the action. Answers: *why is this being done?* |

### Attribute Ordering

Attributes must appear in the order: `goal`, then `rationale`. This ordering reflects natural cognitive flow and optimizes LLM inference:

1. **Cognition mirrors generation** — When reasoning about action, the mental sequence is *what am I doing* → *why am I doing it*. Goal is the anchor; rationale is elaboration.

2. **Token commitment** — During autoregressive generation, earlier tokens constrain later ones. Writing goal first locks in the outcome, forcing the rationale to cohere with it. Rationale-first risks post-hoc rationalization of vague justifications.

3. **Natural speech patterns** — Humans report actions as "I'm doing X because Y" not "Because Y, I'm doing X" (which carries defensive connotations).

### The Distinction Between Goal and Rationale

**Goal** is *what* — declarative, outcome-focused, terse. It reads like a status update or function name in natural language.

**Rationale** is *why* — explanatory, contextual, reasoning-oriented. It invites justification and captures the decision-making process.

| Aspect | Goal | Rationale |
|--------|------|-----------|
| Question answered | What is happening? | Why is this justified? |
| Cognitive mode | Declarative | Explanatory |
| Length | Terse | Expanded |
| Tone | Confident | Reflective |
| Analogous to | Task manager entry | Audit log annotation |

This distinction serves different consumption contexts:

- **Dashboard (scanning)** — Goal only. Users ask "what's happening?" and need glanceable status.
- **Detail view (investigating)** — Goal + rationale. Users want to understand reasoning.
- **Logs and history (auditing)** — Both attributes plus code. Complete record for metacognitive review.

### Phenomenological Grounding

The term "intent" draws from phenomenology's concept of *intentionality* — the intrinsic directedness of consciousness toward objects. An intent is not merely a command but a cognitive act with structure:

- **Noetic aspect** — The act of intending (captured in the code)
- **Noematic aspect** — The intended object or state (captured in the goal)
- **Telos** — The fulfillment condition toward which the intent is directed

Using `goal` rather than `purpose` avoids conflating the *toward-what* of intentional structure with the *why* of instrumental rationality. Goals describe directedness; rationales explain reasons.

### Examples

#### Knowledge Graph Operations

```xml
<golem:intent goal="Find all concepts connected to 'metacognition' within 2 hops">
memory.query("MATCH (n)-[*1..2]-(m) WHERE n.name = 'metacognition' RETURN m").collect {
    // ... output
}
</golem:intent>
```

```xml
<golem:intent goal="Create relationship between research question and discovered papers"
              rationale="Persist discovered relevance for future retrieval and citation tracking">
  graph.execute("""
    MATCH (q:Question {id: '$questionId'}), (p:Paper {id: '$paperId'})
    CREATE (q)-[:RELEVANT_TO {score: $score, discovered: datetime()}]->(p)
  """)
</golem:intent>
```

#### Machine Learning

```xml
<golem:intent goal="Fine-tune embedding model on recent conversation patterns"
              rationale="Improve semantic similarity detection for recurring user interests">
  trainer.finetune(
    model = embeddings.current(),
    dataset = conversations.recent(days = 30).toTrainingSet(),
    epochs = 3
  )
</golem:intent>
```

```xml
<golem:intent goal="Train classifier to detect intent boundaries in conversation"
              rationale="Current heuristics miss nested intents; learned model should generalize better">
  val model = IntentBoundaryClassifier()
  model.train(labeledConversations)
  model.save("intent-boundary-v2")
</golem:intent>
```

#### Self-Modification

```xml
<golem:intent goal="Update prompt template for code generation tasks"
              rationale="Recent failures suggest current template lacks sufficient context about error handling">
  templates.update("code-generation") {
    append(errorHandlingGuidelines)
    version = version.increment()
  }
</golem:intent>
```

```xml
<golem:intent goal="Adjust retrieval threshold from 0.7 to 0.8"
              rationale="Too many marginally relevant memories surfacing, diluting response quality">
  config.memory.retrievalThreshold = 0.8
  config.save()
  log.info("Retrieval threshold adjusted based on quality assessment")
</golem:intent>
```

#### Data Analysis

```xml
<golem:intent goal="Compute distribution of response latencies over past week"
              rationale="User mentioned slowness; need empirical data before investigating">
  val latencies = metrics.responseTimes(since = now() - 7.days)
  latencies.describe().also { println(it) }
</golem:intent>
```

```xml
<golem:intent goal="Cluster recent queries by semantic similarity"
              rationale="Identify recurring themes to prioritize knowledge graph expansion">
  val clusters = queries.recent(100)
    .embed()
    .cluster(algorithm = HDBSCAN, minClusterSize = 5)
  clusters.summarize().store("query-themes")
</golem:intent>
```

#### Environment Interaction

```xml
<golem:intent goal="List all files in /tmp folder">
  files.list("/tmp").forEach { println(it) }
</golem:intent>
```

```xml
<golem:intent goal="Create backup of current configuration"
              rationale="Preparing for experimental parameter changes; ensure rollback capability">
  config.backup(tag = "pre-experiment-${timestamp()}")
</golem:intent>
```

### Processing and Lifecycle

1. **Generation** — LLM produces intent markup as part of reasoning output
2. **Parsing** — Golem extracts goal, rationale, and code from the markup
3. **Display** — Goal appears in dashboard; rationale available on expansion
4. **Execution** — Kotlin Script code runs in sandboxed environment
5. **Recording** — Full intent (goal + rationale + code + result) persists to knowledge graph
6. **Review** — Golem or human operator can audit reasoning chains via stored intents

### Dashboard Presentation

When displaying active or completed intents to users:

- **List view**: Show `goal` only — scannable, status-oriented
- **Expanded view**: Show `goal` + `rationale` — full reasoning context
- **History/audit view**: Show all attributes + code + execution result

The goal attribute is optimized for glanceability. It should read like a confident status update, not a defensive explanation.