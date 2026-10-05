# ADR-070: Linguistic tools architecture

Status: Accepted

Date: 2026-10-05

## Context

Published dictionary, morphology, grammar, search, and S6 knowledge are readable, but the visitor still has to open each module separately.

## Decision

Add a `tools` module that orchestrates those application ports. The morphology engine stays in `morphology`. Knowledge modules do not depend on `tools`. The public catalog is a fixed list, not a plugin framework.

## Consequences

ArchUnit rejects a dependency from a knowledge module onto `tools`, and a dependency from `tools` onto foreign infrastructure.
