import assert from "node:assert/strict";
import test from "node:test";
import {
  attributionLine,
  canonicalSlug,
  emptyLookupMessage,
  licenseNeedsWarning,
  searchPath,
  wordDescription,
  wordTitle,
} from "../lib/dictionary.ts";
import { visibleAdminNav } from "../lib/admin-nav.ts";

test("arabic slugs survive one extra encoding", () => {
  assert.equal(canonicalSlug("كتاب-a2101cbb"), "كتاب-a2101cbb");
  assert.equal(canonicalSlug(encodeURIComponent("كتاب-a2101cbb")), "كتاب-a2101cbb");
});

test("homepage search builds a dictionary query", () => {
  assert.equal(searchPath("كتاب"), "/search?q=%D9%83%D8%AA%D8%A7%D8%A8");
  assert.equal(emptyLookupMessage, "لم نعثر على مدخل منشور لهذه الكلمة.");
});

test("word metadata uses a published meaning and does not invent one", () => {
  assert.equal(wordTitle("كتاب"), "كتاب — المعنى والجذر");
  assert.equal(wordDescription("صحيفة مجموعة"), "صحيفة مجموعة");
  assert.equal(wordDescription("  "), undefined);
  assert.equal(wordDescription(null), undefined);
});

test("source attribution keeps the public citation fields", () => {
  assert.equal(
    attributionLine({ title: "مقاييس اللغة", author: "ابن فارس", edition: "الأولى", pageFrom: 120 }),
    "مقاييس اللغة · ابن فارس · الأولى · ص 120",
  );
});

test("restricted and unknown licenses stay visibly blocked", () => {
  assert.equal(licenseNeedsWarning("RESTRICTED"), true);
  assert.equal(licenseNeedsWarning("UNKNOWN"), true);
  assert.equal(licenseNeedsWarning("CC_BY"), false);
});

test("dictionary navigation follows editorial permissions", () => {
  const reviewer = visibleAdminNav(["dictionary.entry.view", "dictionary.entry.review", "dictionary.root.view"]).map((item) => item.href);
  assert.equal(reviewer.includes("/admin/dictionary"), true);
  assert.equal(reviewer.includes("/admin/review"), true);
  assert.equal(reviewer.includes("/admin/sources"), false);
  const editor = visibleAdminNav(["dictionary.entry.view", "source.view"]);
  assert.equal(editor.some((item) => item.href === "/admin/sources"), true);
  assert.equal(editor.some((item) => item.href === "/admin/review"), false);
});
