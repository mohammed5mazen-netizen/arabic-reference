import assert from "node:assert/strict";
import test from "node:test";
import { visibleAdminNav } from "../lib/admin-nav.ts";
import { textDirection } from "../lib/site.ts";
import {
  dictionaryCard,
  emptySearchMessage,
  highlightSegments,
  matchReasonLabel,
  rebuildConfirmation,
  resultTypeLabel,
  searchDebounceMs,
  searchFilterLayout,
  searchHref,
  showsScore,
  suggestionIndex,
  suggestionUrl,
  type SearchHit,
} from "../lib/search.ts";

test("homepage suggestions use the unified endpoint after a short pause", () => {
  assert.equal(suggestionUrl("ك"), null);
  assert.match(suggestionUrl("كتاب") ?? "", /\/api\/v1\/public\/search\/suggestions\?q=/);
  assert.ok(searchDebounceMs >= 200);
});

test("suggestion keyboard moves and closes", () => {
  assert.equal(suggestionIndex(-1, "ArrowDown", 3), 0);
  assert.equal(suggestionIndex(0, "ArrowDown", 3), 1);
  assert.equal(suggestionIndex(2, "ArrowDown", 3), 0);
  assert.equal(suggestionIndex(0, "ArrowUp", 3), 2);
  assert.equal(suggestionIndex(1, "Escape", 3), "close");
  assert.equal(suggestionIndex(1, "Enter", 3), "choose");
});

test("search links keep the query, filter, and page", () => {
  assert.equal(searchHref("كتاب", "dictionary", 2), "/search?q=%D9%83%D8%AA%D8%A7%D8%A8&type=dictionary&page=2");
  assert.equal(searchHref("كتاب"), "/search?q=%D9%83%D8%AA%D8%A7%D8%A8");
});

test("result cards stay textual and do not expose a score", () => {
  const hit: SearchHit = {
    type: "DICTIONARY_ENTRY",
    id: "1",
    title: "كتاب",
    subtitle: "كِتاب",
    snippet: "ما يُكتب فيه",
    url: "/word/kitab",
    matchReason: "NORMALIZED_EXACT",
    highlights: [],
    metadata: { partOfSpeech: "NOUN", root: "كتب" },
  };
  assert.equal(dictionaryCard(hit).root, "الجذر: كتب");
  assert.equal(matchReasonLabel("NORMALIZED_EXACT"), "مطابق بعد التطبيع");
  assert.equal(matchReasonLabel("EXACT"), null);
  assert.equal(matchReasonLabel("MORPHOLOGY"), "تطابق صرفي");
  assert.equal(matchReasonLabel("FUZZY"), "نتيجة قريبة");
  assert.equal(resultTypeLabel("ROOT"), "جذر");
  assert.equal(resultTypeLabel("GRAMMAR_RULE"), "قاعدة نحوية");
  assert.equal(resultTypeLabel("GRAMMAR_CONCEPT"), "مصطلح");
  assert.equal(showsScore(hit), false);
  assert.equal(emptySearchMessage, "لم نعثر على نتائج مطابقة.");
});

test("highlights are text segments, including hostile input", () => {
  const hostile = "<img src=x onerror=alert(1)>";
  const segments = highlightSegments(hostile, []);
  assert.equal(segments.map((part) => part.text).join(""), hostile);
  assert.equal(segments.some((part) => part.text.includes("<mark>")), false);
  const marked = highlightSegments("كتاب", [{ field: "title", start: 0, end: 2 }]);
  assert.equal(marked[0].highlighted, true);
  assert.equal(marked[0].text, "كت");
});

test("filters are a horizontal control and the page stays right to left", () => {
  assert.equal(textDirection, "rtl");
  assert.equal(searchFilterLayout, "scroll-row");
});

test("admin search is permission aware and rebuild asks first", () => {
  assert.equal(visibleAdminNav(["search.admin.view"]).some((item) => item.href === "/admin/search"), true);
  assert.equal(visibleAdminNav(["dictionary.entry.view"]).some((item) => item.href === "/admin/search"), false);
  assert.match(rebuildConfirmation(), /المحتوى المنشور/);
});
