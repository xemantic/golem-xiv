// V001 - Initial Golem Schema
// Establishes constraints and indexes for the Golem XIV knowledge graph schema.
//
// Node labels:
//   Cognition            - A reasoning session
//   PhenomenalExpression - A unit of expression within a cognition
//   EpistemicAgent       - An entity that can express phenomena (AI, Human, Computer)
//   Phenomenon           - A unit of perception/expression (Text, Image, Document, Intent, Fulfillment)
//
// Relationships:
//   (Cognition)-[:hasPart]->(PhenomenalExpression)
//   (Cognition)-[:hasChild]->(Cognition)
//   (EpistemicAgent)-[:creator]->(PhenomenalExpression)
//   (PhenomenalExpression)-[:hasPart]->(Phenomenon)
//   (Phenomenon:Fulfillment)-[:fulfills]->(Phenomenon:Intent)
//   (Phenomenon:Fulfillment)-[:actualizes]->(*)

// --- Cognition ---

// Index on initiationMoment for temporal queries and ordering
CREATE INDEX cognition_initiation_moment_index IF NOT EXISTS
FOR (n:Cognition) ON (n.initiationMoment);

// Index on title for search
CREATE INDEX cognition_title_index IF NOT EXISTS
FOR (n:Cognition) ON (n.title);

// --- PhenomenalExpression ---

// Index on initiationMoment for temporal queries and ordering
CREATE INDEX phenomenal_expression_initiation_moment_index IF NOT EXISTS
FOR (n:PhenomenalExpression) ON (n.initiationMoment);

// --- EpistemicAgent ---

// Index on initiationMoment for all epistemic agents
CREATE INDEX epistemic_agent_initiation_moment_index IF NOT EXISTS
FOR (n:EpistemicAgent) ON (n.initiationMoment);

// Index on model for AI agent lookups (vendor + model together identify an AI model)
CREATE INDEX epistemic_agent_ai_model_index IF NOT EXISTS
FOR (n:EpistemicAgent) ON (n.model);

// --- Phenomenon ---

// Index on systemId for Intent lookups (GolemScript tool call ID)
CREATE INDEX phenomenon_intent_system_id_index IF NOT EXISTS
FOR (n:Phenomenon) ON (n.systemId);
