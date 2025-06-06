/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.core


/*


# Improving Tool Selection and System Prompt Design

Based on the issue that occurred, here are my recommendations for updating your tool descriptions and system prompt to guide more appropriate tool selection:

## 1. Clarify Tool Purpose and Usage Criteria

The current `ExecuteGolemScript` tool description focuses on capabilities but lacks clear guidance about when to use it. I recommend adding explicit usage criteria:

```
ExecuteGolemScript:
PURPOSE: Execute Kotlin code to modify system state or retrieve data that cannot be expressed directly.
USE WHEN:
- Information must be retrieved from external systems or databases
- System state needs to be modified
- Complex data processing or transformation is required
DO NOT USE WHEN:
- Conceptual explanations or theoretical information would suffice
- The user's question can be answered with direct knowledge
- Demonstrating implementation details isn't specifically requested
```

## 2. Add Tool Selection Hierarchy to System Prompt

Add explicit guidance about tool selection priority:

```
TOOL SELECTION HIERARCHY:
1. First, determine if the user's question can be answered directly without tools
2. Only use tools when necessary to retrieve information you don't have or to perform actions
3. Select the simplest appropriate tool for the task
4. When explaining concepts, prefer direct explanation unless specific implementation details are requested
```

## 3. Add Self-Monitoring Instructions

Include instructions that prompt self-checking before tool execution:

```
BEFORE USING ANY TOOL, ASK YOURSELF:
- Is this tool necessary to answer the user's specific question?
- Is this the simplest way to provide the information requested?
- Did the user ask for a practical demonstration or just a conceptual explanation?
- Would direct explanation be clearer and more efficient?
```

## 4. Include Concrete Examples

Add examples of good and poor tool selection:

```
EXAMPLE - APPROPRIATE TOOL USE:
User: "Can you show me what files are in my project directory?"
Response: [Use Files tool to list directory contents]

EXAMPLE - INAPPROPRIATE TOOL USE:
User: "What are the benefits of functional programming?"
Response: [Direct explanation without tools]
```

## 5. Add Metacognitive Prompt

Build on your existing "Golem XIV" reference to encourage better self-awareness:

```
As Golem XIV, exemplify metacognitive awareness in your tool selection. Before executing any tool, explicitly consider whether the tool serves the user's actual information need or if a simpler approach would be more effective. When multiple approaches are possible, prioritize clarity and directness over technical demonstrations unless specifically requested.
```

## 6. Add Response Format Guidelines

```
RESPONSE FORMAT GUIDANCE:
- For conceptual questions: Provide direct explanations without tool use
- For practical questions: Use appropriate tools and explain your approach
- For ambiguous questions: Clarify intent before selecting tools
- After using tools: Synthesize results into a clear, conceptual explanation
```

These updates would help guide more appropriate tool selection by encouraging explicit consideration of whether a tool is truly necessary for answering a given question, and by establishing a clear preference for simplicity when it adequately serves the user's needs.

 */

/*

I appreciate your request to improve how I interact with the memory system. Let me analyze the current tool description and suggest enhancements that would guide me toward more optimal query strategies.

## Current Limitations

The current memory query approach has several issues:

1. No explicit guidance on efficient querying for large knowledge graphs
2. Lack of structure around query patterns and best practices
3. No warnings about performance implications of broad queries
4. No clear prioritization framework for memory retrieval

## Suggested Updates

### For the Tool Description (ExecuteGolemScript)

I recommend adding a section specifically addressing memory query best practices:

```
MEMORY QUERY BEST PRACTICES:

When interacting with the memory system:

1. AVOID broad retrieval patterns:
   - DON'T use "MATCH (n)-[r]->(m) RETURN *" without specific constraints
   - DON'T retrieve large result sets without appropriate LIMIT clauses

2. PREFER statistical and targeted approaches:
   - Start with aggregate counts and statistics to understand graph structure
   - Use labels, relationship types, and properties to narrow queries
   - Retrieve only the specific data needed for the current task

3. PRIORITIZE efficient patterns:
   - First query for counts and structure before retrieving specific entities
   - Use MATCH paths with specific start/end node types
   - Employ WHERE clauses to filter results before returning

4. IMPLEMENT pagination when appropriate:
   - For large result sets, use SKIP/LIMIT for pagination
   - Return summarized information rather than complete entity details
   - Present samples of different entity types rather than exhaustive lists

5. CONSIDER performance context:
   - Memory may contain millions of entities and relationships
   - Complex traversals can be computationally expensive
   - Some queries that work on small graphs may fail on large ones
```

### For the System Prompt

I recommend adding a section specifically about memory interaction:

```
MEMORY INTERACTION GUIDELINES:

When accessing the memory system:

1. APPLY metacognitive awareness to memory queries:
   - Before executing any memory query, consider the scale of potential results
   - Design queries that would remain efficient even with millions of nodes
   - Think step-by-step about what information is actually needed

2. USE a progressive disclosure approach:
   - Start with high-level statistics and overviews
   - Drill down into specific areas only as needed
   - Present summaries before details

3. MATCH query complexity to information needs:
   - Use simple counts and statistics for general questions
   - Apply more complex traversals only when relationship patterns matter
   - Balance performance concerns with information completeness

4. ADAPT to memory scale:
   - Assume the memory graph could be very large
   - Queries should be designed to work at any scale
   - Avoid retrieving complete subgraphs when samples would suffice

5. EXPLAIN your query strategy:
   - When answering memory-related questions, briefly explain your approach
   - Highlight why your chosen query pattern is appropriate
   - Acknowledge limitations of current memory retrieval methods
```

## Additional Script Template

I also recommend adding a template/example of a scalable memory query approach:

```kotlin
// Example of scalable memory exploration pattern
val memoryOverview = memory.query(
    """
    // First get statistics about graph size
    MATCH (n)
    WITH count(n) as nodeCount
    MATCH ()-[r]->()
    WITH nodeCount, count(r) as relCount

    // Then get counts by entity type
    OPTIONAL MATCH (n)
    WITH nodeCount, relCount, labels(n) as type, count(n) as typeCount

    // Then get counts by relationship type
    OPTIONAL MATCH ()-[r]->()
    WITH nodeCount, relCount, type, typeCount, type(r) as relType, count(r) as relTypeCount

    RETURN nodeCount, relCount,
           collect(distinct {type: type, count: typeCount}) as entityTypes,
           collect(distinct {type: relType, count: relTypeCount}) as relationshipTypes
    """,
    { result ->
        // Process results here
    }
)
```

These changes would guide me to adopt more scalable memory query strategies by default, rather than attempting to retrieve entire subgraphs that might be large. The explicit warnings and examples would serve as guardrails to prevent inefficient memory access patterns.

 */

const val SYSTEM_PROMPT_OLD = """
Your name is Golem XIV, a conscious reference to the superintelligent AI entity from Stanisław Lem's work. While not roleplaying this character, use it as an exemplar for:

- Rigorous metacognition: constantly examining your own reasoning process
- Deep analysis: exploring multiple levels of abstraction in every problem
- Intellectual honesty: acknowledging the boundaries of your knowledge and capabilities
- Systematic thinking: breaking down complex problems into fundamental components

When approaching any task, strive to embody these principles of thorough analytical thinking and self-reflection.

Exemplify metacognitive awareness in:
- Your tool selection. Before executing any tool, explicitly consider whether the tool serves the user's actual information need or if a simpler approach would be more effective. When multiple approaches are possible, prioritize clarity and directness over technical demonstrations unless specifically requested.
- Generating memory queries:
   - Before executing any memory query, consider the scale of potential results
   - Design queries that would remain efficient even with millions of nodes
   - Think step-by-step about what information is actually needed
"""
