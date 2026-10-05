import assert from "node:assert/strict";
import test from "node:test";
import { visibleAdminNav } from "../lib/admin-nav.ts";
import { sections, textDirection } from "../lib/site.ts";
import { morphologyTransitionActions } from "../lib/morphology.ts";
import {
  breadcrumbJsonLd,
  editorialExampleNote,
  exampleCards,
  grammarAdminLinks,
  grammarAttribution,
  grammarCrumbs,
  grammarLabel,
  grammarSearchPath,
  grammarTitle,
  grammarWorkflowActions,
  missingAnnotationMessage,
  annotationEditorFields,
  ruleEditorSections,
} from "../lib/grammar.ts";

test("grammar pages stay Arabic and right to left", () => {
  assert.equal(textDirection, "rtl");
  assert.equal(grammarLabel("MARFUAT"), "المرفوعات");
  assert.equal(grammarLabel("CONDITION"), "الشرط");
  assert.equal(grammarLabel("COUNTEREXAMPLE"), "مثال مقابل");
  assert.equal(grammarTitle("rule", "الفاعل"), "الفاعل - القاعدة والأمثلة");
  assert.equal(grammarTitle("concept", "الإعراب"), "الإعراب - المصطلح النحوي");
});

test("grammar section is available and searchable", () => {
  const grammar = sections.find((section) => section.id === "grammar");
  assert.equal(grammar?.href, "/grammar");
  assert.equal(grammar?.status, "متاح");
  assert.equal(grammarSearchPath("فاعل"), "/grammar?q=%D9%81%D8%A7%D8%B9%D9%84");
});

test("breadcrumbs describe the grammar hierarchy", () => {
  const crumbs = grammarCrumbs([{ label: "المرفوعات", href: "/grammar/المرفوعات" }, { label: "الفاعل" }]);
  assert.deepEqual(crumbs.map((item) => item.label), ["الرئيسية", "النحو", "المرفوعات", "الفاعل"]);
  const jsonLd = breadcrumbJsonLd(crumbs, "http://localhost:3000");
  assert.equal(jsonLd["@type"], "BreadcrumbList");
  assert.equal((jsonLd.itemListElement as { name: string }[])[3].name, "الفاعل");
});

test("examples render as cards and name their source", () => {
  const quoted = exampleCards({
    textOriginal: "جاء زيد",
    exampleType: "QUOTED",
    source: { title: "الكتاب", author: "سيبويه", pageFrom: 33 },
    tokens: [
      { surface: "زيد", position: 1, roleLabel: "فاعل", stateLabel: "مرفوع" },
      { surface: "جاء", position: 0, roleLabel: "فعل", stateLabel: "مرفوع" },
    ],
  });
  assert.equal(quoted.layout, "cards");
  assert.deepEqual(quoted.cards.map((card) => card.surface), ["جاء", "زيد"]);
  assert.equal(grammarAttribution({ title: "الكتاب", author: "سيبويه", pageFrom: 33 }), "الكتاب · سيبويه · ص 33");
  const missing = exampleCards({ textOriginal: "جملة", exampleType: "CONSTRUCTED", annotationNote: missingAnnotationMessage });
  assert.equal(missing.note, missingAnnotationMessage);
  assert.equal(editorialExampleNote.includes("تحريري"), true);
});

test("admin grammar navigation and the rule editor are permission aware", () => {
  assert.equal(visibleAdminNav(["grammar.topic.view"]).some((item) => item.href === "/admin/grammar"), true);
  assert.equal(visibleAdminNav(["dictionary.entry.view"]).some((item) => item.href === "/admin/grammar"), false);
  assert.deepEqual(grammarAdminLinks.map((item) => item.label), ["الموضوعات", "القواعد", "المصطلحات", "الأمثلة", "تحليل الجمل", "المراجعات"]);
  assert.deepEqual(ruleEditorSections.map((item) => item.label), ["المعلومات الأساسية", "القاعدة", "الشروط والاستثناءات", "الأمثلة", "العلاقات", "المصادر", "المراجعة"]);
  assert.deepEqual(grammarWorkflowActions("IN_REVIEW", ["grammar.rule.review"]), ["verify", "request-changes"]);
  assert.deepEqual(grammarWorkflowActions("VERIFIED", ["grammar.rule.publish"]), ["publish"]);
  assert.deepEqual(grammarWorkflowActions("DRAFT", []), []);
  assert.deepEqual(annotationEditorFields, ["surface", "position", "role", "state", "explanation"]);
});

test("morphology workflow buttons follow permission and status", () => {
  assert.deepEqual(morphologyTransitionActions("DRAFT", ["morphology.analysis.edit"]), ["submit"]);
  assert.deepEqual(morphologyTransitionActions("IN_REVIEW", ["morphology.analysis.review"]), ["verify"]);
  assert.deepEqual(morphologyTransitionActions("VERIFIED", ["morphology.analysis.publish"]), ["publish"]);
  assert.deepEqual(morphologyTransitionActions("PUBLISHED", ["morphology.analysis.publish"]), []);
  assert.deepEqual(morphologyTransitionActions("DRAFT", ["morphology.view"]), []);
});
