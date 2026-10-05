# ADR-049: Public grammar URL strategy

Status: Accepted

Date: 2026-10-03

## Context

Grammar pages must be indexable, stable, and readable in Arabic, including when two topics share a title.

## Decision

Public routes are `/grammar`, `/grammar/{slug}`, `/grammar/rules/{slug}`, and `/grammar/concepts/{slug}`. The slug is the stable `ContentSlugs` value, not the raw title. Page titles are specific, such as `الفاعل - القاعدة والأمثلة`, and the site template appends the site name. Breadcrumbs and `BreadcrumbList` follow the published ancestors. The sitemap stays the homepage until S12.

## Consequences

A renamed rule keeps its URL. Search results link to the kind-specific route. Draft and archived slugs return 404.
