# ADR-041: Grammar topic hierarchy

Status: Accepted

Date: 2026-10-03

## Context

Arabic grammar is taught as a tree, for example grammar, then the marfuat, then the subject.

## Decision

`GrammarTopic.parentId` is nullable. The database rejects a topic as its own parent. The application locks the topic and the ancestor chain in id order, then rejects a cycle, including two simultaneous moves that would point at each other. Prerequisites use a separate cycle check.

## Consequences

The public topic page can show children and ancestors from published records. A draft parent change does not move the public node until the next publication, because public children use `published_parent_id`.
