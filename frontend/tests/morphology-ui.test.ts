import assert from "node:assert/strict";
import test from "node:test";
import { visibleAdminNav } from "../lib/admin-nav.ts";
import { featureLines, morphologyLabel, segmentationLine, type MorphCandidate } from "../lib/morphology.ts";

test("morphology labels stay in Arabic", () => {
  assert.equal(morphologyLabel("ACTIVE_PARTICIPLE"), "اسم فاعل");
  assert.equal(morphologyLabel("PASSIVE_PARTICIPLE"), "اسم مفعول");
  assert.equal(morphologyLabel("SINGULAR"), "مفرد");
  assert.equal(morphologyLabel("DUAL"), "مثنى");
  assert.equal(morphologyLabel("MANUAL_VERIFIED"), "موثق يدويًا");
  assert.equal(morphologyLabel("EXACT_DICTIONARY"), "مطابق للمعجم");
  assert.equal(morphologyLabel("RULE_DERIVED"), "مستنتج بقاعدة");
  assert.equal(morphologyLabel("AMBIGUOUS"), "تحليل محتمل");
});

test("feature lines keep dual and do not invent a case", () => {
  assert.deepEqual(featureLines({ number: "DUAL", gender: "MASCULINE" }), ["العدد: مثنى", "الجنس: مذكر"]);
  assert.deepEqual(featureLines({}), []);
});

test("segmentation stays readable on a narrow card", () => {
  const candidate = {
    surfaceForm: "والكتاب",
    normalizedForm: "والكتاب",
    provenance: "EXACT_DICTIONARY",
    explanationCodes: [],
    segmentation: { clitics: ["و"], prefixes: ["ال"], stem: "كتاب", suffixes: [] },
  } satisfies MorphCandidate;
  assert.equal(segmentationLine(candidate), "و + ال + كتاب");
});

test("morphology admin navigation follows the view permission", () => {
  assert.equal(visibleAdminNav(["morphology.view"]).some((item) => item.href === "/admin/morphology"), true);
  assert.equal(visibleAdminNav(["dictionary.entry.view"]).some((item) => item.href === "/admin/morphology"), false);
});
