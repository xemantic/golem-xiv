# Golem Markup Language

This document specifies the markup language used within the Golem XIV cognitive architecture to describe cognitive phenomena — perceptions, expressions, intentions, and their metadata.

## Beyond the Message-Role Paradigm

Contemporary LLM APIs (OpenAI, Anthropic, and others) structure communication using a message-role paradigm inherited from communication theory: messages carry a `role` attribute (`user`, `assistant`, `system`) that identifies the speaker within a turn-taking conversation. This framing treats cognition as dialogue — an exchange of utterances between discrete participants.

Golem Markup Language reframes this paradigm through the lens of cognitive science and philosophy of mind. Rather than modeling *who said what*, it models *what was perceived* and *what was expressed* — the phenomenal structure of cognitive acts. This shift enables:

1. **Epistemic agent attribution** — A message with `role="user"` in the underlying API might actually originate from a cloud virtual machine Golem instantiated, another AI system, or a sensor feed. The `<golem:expression>` element captures the true epistemic source, preserving the cognitive provenance that role-based systems flatten.

2. **Resource description** — Artifacts encountered during cognition (files, API responses, database results) are not merely "user messages" but perceived objects with their own metadata. The `<golem:resource>` element describes these resources within the cognitive stream.

3. **Intentional structure** — The `<golem:intent>` element captures the directedness of cognitive acts — the coupling of *what is to be achieved* with *why* and *how*. This bridges natural language reasoning and executable code while maintaining auditability.

The markup language thus serves as a phenomenological annotation layer that preserves cognitive structure when interfacing with role-based LLM APIs.

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

### When Rationale May Be Omitted

Rationale is optional for actions where the *why* is self-evident from the goal itself. Omitting rationale signals:

1. **Transparent causality** — The goal directly implies its justification. "List all files in /tmp folder" needs no explanation; the intent is fully captured in the goal.

2. **Immediate context** — The action responds to an explicit user request or follows deterministically from prior reasoning visible in the conversation.

3. **Low decision complexity** — No alternatives were weighed, no tradeoffs considered. The action is mechanical rather than deliberative.

Conversely, rationale should be present when:

- The action involves a **choice** among alternatives
- The reasoning depends on **context not visible** in the goal
- The action has **side effects** that warrant justification
- **Future review** (by Golem or humans) would benefit from understanding the decision

When rationale is absent, dashboard and audit views should treat it as "self-evident from goal" rather than "reasoning missing." An absent rationale is not a defect — it's a signal that the action requires no further explanation.

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
    graph.query("MATCH (n)-[*1..2]-(m) WHERE n.name = 'metacognition' RETURN m")
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

## `<golem:expression>`

Expression collects perceivable phenomena including language representation of LLMs cognitive process.

Epistemic agent types:

- `self` (Golem XIV itself)
- `human` (A human user initiator)
- `ai` (Another AI system initiator)

### Examples

```xml
<golem:expression id="" epistemic-agent-id="" type="self" initiation-moment="">
    
</golem:expression>
```

## `<golem:resource>`

### Examples

#### file

```xml
<golem:resource uri="computer:machine1:/etc/passwd" from-line="10" to-line="12">
    
</golem:resource>
```
