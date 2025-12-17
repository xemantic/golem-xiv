// Initial schema: Create Person node with basic constraints and indexes
// This migration establishes the foundation for person management

// Create unique constraint on Person.id - ensures data integrity
// This also creates an index automatically for fast lookups by ID
//CREATE CONSTRAINT person_id_unique IF NOT EXISTS
//FOR (p:Person) REQUIRE p.id IS UNIQUE;
//
//// Create index on Person.name for fast text search and filtering
//// Demonstrates common query pattern: finding people by name
//CREATE INDEX person_name_index IF NOT EXISTS
//FOR (p:Person) ON (p.name);
//
//// Create index on Person.createdAt for efficient temporal queries
//// Enables fast sorting and filtering by creation date (pagination, analytics)
//CREATE INDEX person_created_at_index IF NOT EXISTS
//FOR (p:Person) ON (p.createdAt);
