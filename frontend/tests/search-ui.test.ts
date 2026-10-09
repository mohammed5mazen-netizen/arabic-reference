import assert from "node:assert/strict";
import test from "node:test";
import { visibleAdminNav } from "../lib/admin-nav.ts";
import { textDirection } from "../lib/site.ts";
import {
  dictionaryCard,
  emptySearchMessage,
  emptySearchPrompt,
  searchPresentation,
  searchPublic,
  searchUrl,
  unavailableSearchMessage,
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

test("search API uses q with Arabic encoding and preserves filters and pagination", () => {
  const cases = ["كتاب", "لغة", "برمجة"];
  for (const query of cases) {
    const url = searchUrl(query, { type: "dictionary", page: 3, size: 10 });
    assert.ok(url);
    const parsed = new URL(url);
    assert.equal(parsed.pathname, "/api/v1/public/search");
    assert.equal(parsed.searchParams.get("q"), query);
    assert.deepEqual([...parsed.searchParams.keys()], ["q", "type", "page", "size"]);
    assert.equal(parsed.searchParams.get("type"), "dictionary");
    assert.equal(parsed.searchParams.get("page"), "3");
    assert.equal(parsed.searchParams.get("size"), "10");
  }
  assert.equal(searchUrl(" \t "), null);
  assert.equal(emptySearchPrompt, "ابدأ بكتابة كلمة أو جذر أو موضوع للبحث في المرجع.");
});

test("search API unwraps data.items and does not request an empty query", async () => {
  const originalFetch = globalThis.fetch;
  let requestedUrl = "";
  try {
    globalThis.fetch = async (input) => {
      requestedUrl = String(input);
      return new Response(JSON.stringify({
        data: {
          query: "كتاب",
          items: [{
            type: "DICTIONARY_ENTRY",
            id: "entry-1",
            title: "كتاب",
            subtitle: "اسم • الجذر: كتب",
            snippet: "مؤلَّف",
            url: "/word/kitab",
            matchReason: "EXACT",
            highlights: [{ field: "title", start: 0, end: 4 }],
            metadata: { root: "كتب", partOfSpeech: "NOUN", category: "DICTIONARY" },
          }],
          page: 1,
          size: 20,
          total: 1,
          facets: { dictionary: 1, roots: 0, grammar: 0, content: 0, learning: 0 },
        },
        traceId: "trace-safe",
      }), { status: 200, headers: { "content-type": "application/json" } });
    };
    const results = await searchPublic("كتاب");
    assert.ok(results);
    assert.equal(results.items.length, 1);
    assert.equal(results.items[0].title, "كتاب");
    assert.equal(results.items[0].metadata?.root, "كتب");
    assert.equal(new URL(requestedUrl).searchParams.get("q"), "كتاب");
    assert.deepEqual([...new URL(requestedUrl).searchParams.keys()], ["q", "page", "size"]);
    assert.equal(await searchPublic("   "), null);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("search request failures use a safe user-facing state", async () => {
  const originalFetch = globalThis.fetch;
  try {
    globalThis.fetch = async () => new Response(JSON.stringify({ code: "INTERNAL_ERROR" }), { status: 500 });
    await assert.rejects(searchPublic("كتاب"), /500/);
    assert.equal(unavailableSearchMessage, "تعذر إكمال البحث. أعد المحاولة.");
    assert.equal(searchPresentation(null, true), "unavailable");
  } finally {
    globalThis.fetch = originalFetch;
  }
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
  for (const type of [
    "DICTIONARY_ENTRY", "ROOT", "GRAMMAR_RULE", "GRAMMAR_TOPIC", "GRAMMAR_CONCEPT",
    "SPELLING_RULE", "SPELLING_TOPIC", "RHETORIC_DEVICE", "RHETORIC_TOPIC",
    "LITERARY_FIGURE", "LITERARY_WORK", "LITERARY_ERA", "ARTICLE", "LEARNING_PATH", "LESSON",
  ]) {
    assert.ok(resultTypeLabel(type));
  }
  assert.equal(showsScore(hit), false);
  assert.equal(emptySearchMessage, "لم نعثر على نتائج مطابقة.");
  assert.equal(unavailableSearchMessage, "تعذر إكمال البحث. أعد المحاولة.");
  assert.equal(searchPresentation(null, true), "unavailable");
  assert.equal(searchPresentation({ query: "كتاب", items: [], total: 0, page: 1, size: 20, facets: { dictionary: 0, roots: 0, grammar: 0, content: 0 } }, false), "empty");
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
