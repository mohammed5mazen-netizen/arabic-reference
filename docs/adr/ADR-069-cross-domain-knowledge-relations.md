# ADR-069: Cross-domain knowledge relations

Status: Accepted

Date: 2026-10-05

## Context

An article may point at a dictionary entry, a root, a grammar rule, a spelling rule, a rhetoric device, a literary figure, or a literary work. A polymorphic foreign key would couple those tables.

## Decision

`knowledge_relation` stores an owner and a target type plus target id, with no database foreign key. `KnowledgeTargetSource` adapters answer only when the target is published. The content module depends on the port, not on the other modules' repositories. Relations are created by an editor, never inferred.

## Consequences

An unpublished target cannot be linked. A later archive drops that relation from the next article snapshot.
