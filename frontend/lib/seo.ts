import { siteName } from "./site.ts";

const DESCRIPTION_LIMIT = 160;

export function describe(value: string | null | undefined, fallback: string): string {
  const text = (value ?? "").replace(/\s+/g, " ").trim();
  const source = text.length > 0 ? text : fallback.replace(/\s+/g, " ").trim();
  if (source.length <= DESCRIPTION_LIMIT) return source;
  const cut = source.slice(0, DESCRIPTION_LIMIT - 1);
  const space = cut.lastIndexOf(" ");
  return `${(space > 80 ? cut.slice(0, space) : cut).trim()}…`;
}

export function jsonLd(value: unknown): string {
  return JSON.stringify(value).replaceAll("<", "\\u003c").replaceAll(">", "\\u003e").replaceAll("&", "\\u0026");
}

export function websiteJsonLd(url: string): Record<string, unknown> {
  return {
    "@context": "https://schema.org",
    "@type": "WebSite",
    name: siteName,
    url,
    inLanguage: "ar",
  };
}

export function definedTermJsonLd(input: { name: string; description: string; url: string }): Record<string, unknown> {
  return {
    "@context": "https://schema.org",
    "@type": "DefinedTerm",
    name: input.name,
    description: input.description,
    url: input.url,
    inLanguage: "ar",
    inDefinedTermSet: siteName,
  };
}

export function articleJsonLd(input: {
  headline: string;
  description: string;
  url: string;
  publishedAt?: string | null;
  modifiedAt?: string | null;
  author?: string | null;
}): Record<string, unknown> {
  return {
    "@context": "https://schema.org",
    "@type": "Article",
    headline: input.headline,
    description: input.description,
    inLanguage: "ar",
    mainEntityOfPage: input.url,
    ...(input.publishedAt ? { datePublished: input.publishedAt } : {}),
    ...(input.modifiedAt ? { dateModified: input.modifiedAt } : {}),
    ...(input.author ? { author: { "@type": "Person", name: input.author } } : {}),
  };
}

export const corePublicPaths = [
  "/",
  "/grammar",
  "/spelling",
  "/rhetoric",
  "/literature",
  "/articles",
  "/tools",
  "/tools/morphology",
  "/tools/root",
  "/tools/derivations",
  "/tools/patterns",
  "/tools/word-analysis",
  "/tools/compare",
  "/tools/relations",
  "/tools/spelling-check",
  "/tools/grammar",
  "/tools/explore",
  "/learn",
  "/assistant",
] as const;

export function sitemapPathAllowed(path: string): boolean {
  if (!path.startsWith("/") || path.includes("?") || path.includes("#") || path.includes("%25")) return false;
  if (path.startsWith("/admin") || path.startsWith("/api") || path.startsWith("/search")) return false;
  return !path.includes("/attempts");
}
