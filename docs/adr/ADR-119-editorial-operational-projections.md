# ADR-119: Editorial operational projections

Status: Accepted

Date: 2026-10-09

## Context

Dashboard and queue counts must be real and must stay bounded. Loading every aggregate into Java would not.

## Decision

Status counts, ready-to-publish counts, queue pages, and finding lists are SQL. Content type names come from an enum, and table names used for snapshots come from that enum. Request parameters never become column names. Search consistency reuses the published-search and index ports.

## Consequences

The operations room can lag a domain write only by the database transaction that committed it. It does not keep a second copy of the linguistic record.
