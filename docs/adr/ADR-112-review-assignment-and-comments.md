# ADR-112: Review assignment and comments

Status: Accepted

Date: 2026-10-09

## Context

Requesting a change needed a durable internal note, and two people needed a way to name the reviewer without a project-management tool.

## Decision

`editorial_assignment` stores one reviewer and one optional publisher per record, with a version. The assignee must hold the matching permission, and four-eyes still rejects the creator as reviewer or as publisher of their own review. `editorial_comment` stores one note, a kind, and an open or resolved state. Comments require the content version. They are not part of the public API. Reassignment and comment actions are audited. Page views are not.

## Consequences

There is no threaded conversation and no email when a comment is added.
