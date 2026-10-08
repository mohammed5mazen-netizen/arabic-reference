import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";
import { formatNumber } from "../lib/format.ts";
import { learningSlug } from "../lib/learning.ts";
import { publicMetadata } from "../lib/metadata.ts";
import { footerLinks, navigationGroups, uniqueLinks } from "../lib/navigation.ts";
import { resolveSiteUrl } from "../lib/site.ts";

const routes = new Set([
  "/",
  "/search",
  "/grammar",
  "/spelling",
  "/rhetoric",
  "/literature",
  "/articles",
  "/tools",
  "/tools/morphology",
  "/learn",
  "/assistant",
]);

test("navigation links stay public and unique", () => {
  const links = uniqueLinks(footerLinks.concat(navigationGroups.flatMap((group) => group.links)));
  assert.equal(links.length > 0, true);
  for (const link of links) {
    assert.equal(link.href.startsWith("/"), true);
    assert.equal(routes.has(link.href), true, link.href);
    assert.equal(link.href.includes("%25"), false);
  }
  assert.equal(navigationGroups.some((group) => group.links.some((link) => link.href === "/learn")), true);
});

test("arabic slugs decode once and canonicals stay single encoded", () => {
  assert.equal(learningSlug("النحو"), "النحو");
  assert.equal(learningSlug("%D8%A7%D9%84%D9%86%D8%AD%D9%88"), "النحو");
  assert.equal(learningSlug(encodeURIComponent("%D8%A7%D9%84%D9%86%D8%AD%D9%88")), "النحو");
  const previous = process.env.SITE_URL;
  process.env.SITE_URL = "https://reference.example/";
  try {
    const metadata = publicMetadata({ title: "درس", description: "ملخص", path: `/learn/${encodeURIComponent("النحو")}` });
    const canonical = metadata.alternates && "canonical" in metadata.alternates ? String(metadata.alternates.canonical) : "";
    assert.equal(canonical, "https://reference.example/learn/%D8%A7%D9%84%D9%86%D8%AD%D9%88");
    assert.equal(canonical.includes("%25"), false);
    assert.equal(resolveSiteUrl(), "https://reference.example");
  } finally {
    if (previous == null) delete process.env.SITE_URL;
    else process.env.SITE_URL = previous;
  }
});

test("shared experience primitives stay accessible", () => {
  const button = readFileSync(new URL("../components/ui/button.tsx", import.meta.url), "utf8");
  const empty = readFileSync(new URL("../components/ui/empty-state.tsx", import.meta.url), "utf8");
  const error = readFileSync(new URL("../components/ui/error-state.tsx", import.meta.url), "utf8");
  const badge = readFileSync(new URL("../components/ui/badge.tsx", import.meta.url), "utf8");
  const nav = readFileSync(new URL("../components/site-nav.tsx", import.meta.url), "utf8");
  const layout = readFileSync(new URL("../app/layout.tsx", import.meta.url), "utf8");
  const missing = readFileSync(new URL("../app/not-found.tsx", import.meta.url), "utf8");
  assert.match(button, /disabled:opacity-60/);
  assert.match(empty, /title/);
  assert.match(error, /role="alert"/);
  assert.match(badge, /مثال تعليمي/);
  assert.match(badge, /تحليل محتمل/);
  assert.match(nav, /showModal/);
  assert.match(nav, /إغلاق/);
  assert.match(layout, /تجاوز إلى المحتوى/);
  assert.match(layout, /dir=\{textDirection\}/);
  assert.match(missing, /الصفحة غير موجودة/);
  assert.match(missing, /href="\/search"/);
  assert.equal(formatNumber(12), "12");
});
