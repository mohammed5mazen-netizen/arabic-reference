import assert from "node:assert/strict";
import test from "node:test";
import type { Metadata } from "next";
import { publicMetadata } from "../lib/metadata.ts";
import { articleJsonLd, corePublicPaths, definedTermJsonLd, describe, jsonLd, sitemapPathAllowed, websiteJsonLd } from "../lib/seo.ts";
import { absoluteUrl, canonicalPath, indexableOrigin, indexingEnabled, normalizeSiteUrl, resolveSiteUrl, SiteOriginError } from "../lib/site.ts";

function robotsIndex(metadata: Metadata): boolean | undefined {
  const robots = metadata.robots;
  if (robots == null || typeof robots !== "object" || !("index" in robots)) return undefined;
  return robots.index;
}

function withEnv(values: Record<string, string | undefined>, run: () => void) {
  const previous = new Map<string, string | undefined>();
  for (const [key, value] of Object.entries(values)) {
    previous.set(key, process.env[key]);
    if (value == null) delete process.env[key];
    else process.env[key] = value;
  }
  try {
    run();
  } finally {
    for (const [key, value] of previous) {
      if (value == null) delete process.env[key];
      else process.env[key] = value;
    }
  }
}

test("site origin rejects a query, a fragment, and a path", () => {
  assert.equal(normalizeSiteUrl("https://reference.example/"), "https://reference.example");
  assert.equal(normalizeSiteUrl("http://localhost:3000"), "http://localhost:3000");
  assert.throws(() => normalizeSiteUrl("https://reference.example/ar"), SiteOriginError);
  assert.throws(() => normalizeSiteUrl("https://reference.example/?q=1"), SiteOriginError);
  assert.throws(() => normalizeSiteUrl("https://reference.example/#top"), SiteOriginError);
  assert.equal(indexableOrigin("https://reference.example"), true);
  assert.equal(indexableOrigin("http://reference.example"), false);
  assert.equal(indexableOrigin("https://localhost"), false);
});

test("production resolves its configured or Vercel canonical origin without localhost fallback", () => {
  withEnv({ NODE_ENV: "production", SITE_URL: undefined, NEXT_PUBLIC_SITE_URL: undefined, VERCEL_PROJECT_PRODUCTION_URL: undefined, SEO_INDEXING_ENABLED: undefined, NEXT_PHASE: undefined }, () => {
    assert.throws(() => resolveSiteUrl(), /SITE_URL is required/);
  });
  withEnv({ NODE_ENV: "production", SITE_URL: "http://localhost:3000", SEO_INDEXING_ENABLED: "true", NEXT_PHASE: undefined }, () => {
    assert.throws(() => resolveSiteUrl(), /https SITE_URL/);
  });
  withEnv({ NODE_ENV: "production", SITE_URL: "https://reference.example/", SEO_INDEXING_ENABLED: "false" }, () => {
    assert.equal(resolveSiteUrl(), "https://reference.example");
    assert.equal(indexingEnabled(), false);
  });
  withEnv({ NODE_ENV: "production", SITE_URL: undefined, NEXT_PUBLIC_SITE_URL: undefined, VERCEL_PROJECT_PRODUCTION_URL: "arabic-reference.vercel.app", SEO_INDEXING_ENABLED: "false", NEXT_PHASE: undefined }, () => {
    assert.equal(resolveSiteUrl(), "https://arabic-reference.vercel.app");
  });
});

test("arabic canonicals are encoded once", () => {
  withEnv({ SITE_URL: "https://reference.example", SEO_INDEXING_ENABLED: "true", NODE_ENV: "test" }, () => {
    assert.equal(canonicalPath("/word/%D9%83%D8%AA%D8%A7%D8%A8"), "/word/%D9%83%D8%AA%D8%A7%D8%A8");
    assert.equal(canonicalPath("/word/%25D9%2583%25D8%25AA%25D8%25A7%25D8%25A8"), "/word/%D9%83%D8%AA%D8%A7%D8%A8");
    assert.equal(absoluteUrl("/word/كتاب"), "https://reference.example/word/%D9%83%D8%AA%D8%A7%D8%A8");
    const metadata = publicMetadata({ title: "كتاب — المعنى والجذر", description: "أصل يُرجع إليه في اللغة.", path: "/word/كتاب" });
    const canonical = String(metadata.alternates && "canonical" in metadata.alternates ? metadata.alternates.canonical : "");
    assert.equal(canonical.includes("%25"), false);
    assert.equal(metadata.openGraph?.locale, "ar");
    assert.equal(robotsIndex(metadata), true);
    const hidden = publicMetadata({ title: "البحث", description: "نتائج", path: "/search?q=كتاب", index: false });
    assert.equal(String(hidden.alternates && "canonical" in hidden.alternates ? hidden.alternates.canonical : ""), "https://reference.example/search");
    assert.equal(robotsIndex(hidden), false);
  });
});

test("descriptions stay short and structured data stays textual", () => {
  const long = "كلمة ".repeat(80);
  const description = describe(long, "قصير");
  assert.equal(description.length <= 160, true);
  assert.equal(description.endsWith("…"), true);
  const term = definedTermJsonLd({ name: "كتاب", description: "أصل", url: "https://reference.example/word/كتاب" });
  assert.equal(term["@type"], "DefinedTerm");
  const article = articleJsonLd({ headline: "مقال", description: "ملخص", url: "https://reference.example/articles/a", publishedAt: "2026-10-09T00:00:00Z", modifiedAt: "2026-10-09T00:00:00Z" });
  assert.equal(article["@type"], "Article");
  assert.equal("author" in article, false);
  const site = websiteJsonLd("https://reference.example");
  assert.equal(site.inLanguage, "ar");
  const encoded = jsonLd({ name: "</script><script>alert(1)</script>" });
  assert.equal(encoded.includes("</script>"), false);
  assert.equal(encoded.includes("<"), false);
});

test("sitemap paths keep public knowledge and drop private or query state", () => {
  assert.equal(corePublicPaths.includes("/"), true);
  assert.equal(corePublicPaths.includes("/tools"), true);
  assert.equal(corePublicPaths.includes("/assistant"), true);
  assert.equal(corePublicPaths.includes("/learn"), true);
  assert.equal(corePublicPaths.some((path) => path.startsWith("/search")), false);
  assert.equal(corePublicPaths.some((path) => path.includes("?")), false);
  assert.equal(sitemapPathAllowed("/word/كتاب"), true);
  assert.equal(sitemapPathAllowed("/tools/root?q=كتب"), false);
  assert.equal(sitemapPathAllowed("/admin/seo"), false);
  assert.equal(sitemapPathAllowed("/api/v1/public/discovery"), false);
  assert.equal(sitemapPathAllowed("/search"), false);
});
