import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import path from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";
import { visibleAdminNav } from "../lib/admin-nav.ts";
import { resolveSiteUrl, sections, textDirection } from "../lib/site.ts";
import { featuredTools, linguisticTools, toolMetadata } from "../lib/tools.ts";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");

test("tool catalog stays Arabic and reference-bound", () => {
  assert.equal(textDirection, "rtl");
  assert.equal(linguisticTools.length, 10);
  assert.equal(linguisticTools.some((tool) => tool.name.includes("مدقق إملائي آلي")), false);
  assert.equal(linguisticTools.find((tool) => tool.code === "SPELLING_CHECK")?.name, "التحقق الإملائي المرجعي");
  assert.equal(linguisticTools.find((tool) => tool.code === "MORPHOLOGY")?.route, "/tools/morphology");
  assert.deepEqual(featuredTools.map((tool) => tool.code), ["ROOT", "WORD_ANALYSIS", "COMPARE", "SPELLING_CHECK"]);
  assert.equal(sections.find((section) => section.id === "tools")?.href, "/tools");
});

test("tool pages keep a base canonical and an accessible structure", () => {
  const previous = process.env.SITE_URL;
  process.env.SITE_URL = "https://reference.example/";
  try {
    assert.equal(resolveSiteUrl(), "https://reference.example");
    const metadata = toolMetadata(linguisticTools[1], true);
    assert.equal(metadata.alternates && "canonical" in metadata.alternates ? metadata.alternates.canonical : "", "https://reference.example/tools/root");
    assert.equal(metadata.robots && typeof metadata.robots === "object" && "index" in metadata.robots ? metadata.robots.index : true, false);
  } finally {
    if (previous == null) delete process.env.SITE_URL;
    else process.env.SITE_URL = previous;
  }

  const screen = readFileSync(path.join(root, "components", "tool-screen.tsx"), "utf8");
  const header = readFileSync(path.join(root, "components", "site-header.tsx"), "utf8");
  const grid = readFileSync(path.join(root, "components", "section-grid.tsx"), "utf8");
  const compare = readFileSync(path.join(root, "app", "(public)", "tools", "compare", "page.tsx"), "utf8");
  assert.match(header, /href="\/tools"/);
  assert.match(screen, /htmlFor=/);
  assert.match(screen, /aria-describedby=/);
  assert.match(screen, /role="alert"/);
  assert.match(screen, /grid-cols-1 gap-4 md:grid-cols-2/);
  assert.match(screen, /<ul/);
  assert.match(screen, /history.replaceState/);
  assert.match(compare, /initialLeft/);
  assert.equal(grid.includes("المحلل الصرفي"), false);
  assert.equal(visibleAdminNav(["tools.view"]).some((item) => item.href === "/admin/tools"), true);
  assert.equal(visibleAdminNav(["dictionary.entry.view"]).some((item) => item.href === "/admin/tools"), false);
});
