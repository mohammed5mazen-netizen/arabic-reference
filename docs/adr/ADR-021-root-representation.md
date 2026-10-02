# ADR-021 Root Representation

## Status

Accepted

## Date

2026-10-03

## Context

Arabic roots are usually three or four radicals, with a smaller set of shorter and longer cases. Not every lexical entry has a root.

## Decision

`LinguisticRoot` stores original and normalized letters and the radical count. After normalization, only Arabic letters remain. Counts 3 and 4 are accepted without a note. Counts 2, 5, and 6 require an editorial note. Other lengths are rejected. The normalized root is unique. An entry may leave `rootId` null. S2 does not extract roots automatically.

## Consequences

Loanwords and unresolved entries stay valid. A later morphology stage can suggest a root, and an editor still confirms it.
