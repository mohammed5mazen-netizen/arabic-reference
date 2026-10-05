# ADR-075: Linguistic relation explorer

Status: Accepted

Date: 2026-10-05

## Context

Sense relations, roots, grammar links, and articles can form a long graph, including cycles.

## Decision

The explorer walks published links to depth 2 and at most 50 nodes. Relation types stay the published dictionary and knowledge links. Cycles and duplicate edges are skipped. Unpublished targets are omitted. The interface always includes a text list.

## Consequences

A neighbor of a neighbor is visible. The next hop is not.
