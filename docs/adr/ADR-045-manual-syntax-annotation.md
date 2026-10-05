# ADR-045: Manual syntax annotation

Status: Accepted

Date: 2026-10-03

## Context

I'rab is useful on a grammar example, and an automatic parser would overclaim. Case and mood already exist in the morphology feature model, but grammar must not depend on morphology infrastructure.

## Decision

Annotations, tokens, roles, and dependency edges are manual editorial data. `GrammaticalState` lives in the grammar domain. Roles declare whether they expect a nominal case or a verbal mood. Public retrieval returns a published annotation only. If no published analysis exists, the page says that a verified analysis is unavailable. There is no parse endpoint.

## Consequences

Later automatic analysis can be added beside this store. It cannot replace a missing annotation with a generated one.
