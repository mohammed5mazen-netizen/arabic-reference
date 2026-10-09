# ADR-110: Editorial operations architecture

Status: Accepted

Date: 2026-10-09

## Context

Editors were moving between separate admin screens to see review, publication, sources, and quality. Putting every linguistic entity into one module would break the existing ownership boundaries.

## Decision

`editorial` is an orchestration and read-model module. Knowledge modules keep their entities and publish methods. Editorial reads a SQL view and calls identity and search ports. It does not depend on foreign infrastructure packages, and those modules do not depend on editorial. Publish methods call `PublicationChecks`, which delegates to an optional barrier.

## Consequences

A new content type joins the queue by extending the view and the content-type whitelist. It does not move its tables.
