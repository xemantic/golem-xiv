# Automatic Memory Management

Currently, Golem seems to operate on memory in two ways: one works more like logging (phenomenal events are being saved automatically).
The other works more like a stash, Golem can choose to write or read information from the memory, based on whether it finds it useful.
It happens in the same continous cognition/conversation. To read or write from memory Golem must explicitly decide to do it.

I believe the memory management process should be separate from the main cogntive process. It could be potentially asynchronous, more efficient and cheaper.

## Architecture

### Memory Storage

#### Dual representation

Embeddings:
- "What is this about?"
- Coarse semantic similarity
- On summary/concept nodes only
- Entry point discovery

Knowledge graph:
- "How does this relate?"
- Precise structural relationships
- Full structural detail
- Neighborhood exploration

Retrieval flow:
1. Semantic search finds relevant concept nodes via embeddings
2. Graph traversal expands to connected details
3. Return the subgraph as context

#### Memory Types

Episodic memory: Anchored to specific conversations
- "In cognition X, user expressed phenomenon Y about Z"
- Preserves context and source

Semantic memory: Abstracted, conversation-independent facts
- "Z has property Y"
- Consolidation transforms episodic → semantic when patterns repeat

### Memory consolidation and persistence

1. Golem, at the end of each inference (or when conversation history is big enough), will choose whether to consolidate its short-term memory or not.
    - The consolidation would be handled by a separate, subconscious process.
    - There should be defined guidelines on how should the consolidation behave.
    - The result should also include prepared contextual data for embedding-based index generation.
2. Every consolidation would then prepared by another process to and saved to the memory graph and embeddings for contextual search.
    - Before saving it should be queried to check for existing information.
3. Consolidated conversation's history will also serve as a short-term memory in context window.
    - The history stack can be removed from the context window (we might want to keep the latest turn).
    - Turns that haven't completed consolidation remain in the context window. This prevents race conditions where retrieval misses recently discussed information.

This process could be asynchronous and happen in parallel to normal conversation.
It could also allow for better memory integration, improve inference cost by lowering the context window usage and make the conversation history independent of it.
However, it adds three invisible technical inferences: consolidation, existing memory check queries and memory saving queries.
It might be overkill for smaller tasks, but for long-lasting and extensive tasks it might turn economically advantageous.

### Automatic memory retrieval

I thought of two ways we could approach this:

1. LLM-based
    - A model receives the user's prompt and asks itself: "Do I need any information I could retrieve from the memory?"
    - An output should be a list of questions, each question would be turned into embeddings for a vector search.
2. Rule-based
    - We generate embeddings from extracted information from the user's prompt without the use of LLM.

I imagine the retrieval would happen on every user prompt.
This shouldn't be an issue for the rule-based approach, but for the LLM-based it could be very costly, and it wouldn't make much sense to do it asynchronously.
On the other hand, rule-based approach might not be as accurate, but we could instruct the user and Golem's cognitive processes to more explicitly inform of information they need.
This way I think it would greatly improve the rule-based retreval's accuracy.
Since LLM-based approach would be so expensive, it should be either optional ("deep recall" switch in UI) or ommited completely.

#### Conflict handling

When retrieved long-term memory conflicts with short-term memory or other long-term memories, Golem's conscious process analyzes the discrepancy through reasoning:
- "My memory already has this information, but it's conflicting with what I just learned. I should verify which one is correct."

### Manual memory retrieval

Whenever Golem decides he needs any specific information it could query the database similarily to how it is right now.
I imagine it could leverage the rule-based automatic retrieval or use a tool.

## Additional features (proposed by Claude)

### Memory Decay and Forgetting

- Importance score assigned to memories
- Score decays over time
- Periodic pruning of low-importance, old, unreferenced nodes
- Keeps retrieval fast and relevant

### Source Attribution

Track provenance for each memory:
- Which conversation it originated from
- Whether it was user-stated or inferred
- Confidence level

Useful for conflict resolution and trust calibration.

### Periodic Reorganization

Background process (runs when idle or on schedule):
- Merges redundant nodes
- Identifies and flags contradictions
- Builds higher-level abstractions from repeated patterns
- Transforms episodic → semantic memory

Analogous to sleep consolidation in biological memory.

### Proactive Memory Surfacing

Relevant memories surfaced without explicit retrieval:
- Triggered by high semantic similarity to current context
- Presented explicitly: "This reminds me of something from a previous conversation..."
- Part of automatic retrieval but distinguished in presentation
