import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";
import { citationFinding, emptyReviewMessage, licenseLabel, recordHref, rightsFinding, severityLabel } from "../lib/editorial.ts";
import { visibleAdminNav } from "../lib/admin-nav.ts";

test("editorial labels stay explicit and the operations room is permissioned", () => {
  assert.equal(severityLabel("BLOCKER"), "مانع للنشر");
  assert.equal(severityLabel("WARNING"), "تحذير");
  assert.equal(severityLabel("INFO"), "ملاحظة");
  assert.equal(licenseLabel("RESTRICTED"), "مقيد");
  assert.equal(licenseLabel("UNKNOWN"), "غير معروف");
  assert.equal(emptyReviewMessage, "لا توجد مواد تنتظر المراجعة حاليًا.");
  assert.equal(rightsFinding("RIGHTS_EXCERPT"), true);
  assert.equal(citationFinding("MISSING_CITATION"), true);
  assert.equal(recordHref("DICTIONARY_ENTRY", "abc"), "/admin/editorial/records/DICTIONARY_ENTRY/abc");
  const labels = visibleAdminNav(["editorial.dashboard.view"]).map((item) => item.href);
  assert.equal(labels.includes("/admin/editorial"), true);
  assert.equal(visibleAdminNav(["dictionary.entry.view"]).some((item) => item.href === "/admin/editorial"), false);
});

test("editorial screens keep internal notes off the public site", () => {
  const home = readFileSync(new URL("../app/admin/(desk)/editorial/page.tsx", import.meta.url), "utf8");
  const reviews = readFileSync(new URL("../app/admin/(desk)/editorial/reviews/page.tsx", import.meta.url), "utf8");
  const publishing = readFileSync(new URL("../app/admin/(desk)/editorial/publishing/page.tsx", import.meta.url), "utf8");
  const quality = readFileSync(new URL("../app/admin/(desk)/editorial/quality/page.tsx", import.meta.url), "utf8");
  const sources = readFileSync(new URL("../app/admin/(desk)/sources/page.tsx", import.meta.url), "utf8");
  const record = readFileSync(new URL("../app/admin/(desk)/editorial/records/[type]/[id]/page.tsx", import.meta.url), "utf8");
  const header = readFileSync(new URL("../components/site-header.tsx", import.meta.url), "utf8");
  assert.match(home, /غرفة العمليات التحريرية/);
  assert.match(reviews, /emptyReviewMessage/);
  assert.match(publishing, /موانع الحقوق/);
  assert.match(quality, /مانع للنشر/);
  assert.match(quality, /تحذير/);
  assert.match(quality, /ملاحظة/);
  assert.match(sources, /مصادر محتملة التكرار/);
  assert.match(sources, /مستخدم في/);
  assert.match(record, /md:grid-cols-2/);
  assert.match(record, /التعليقات الداخلية/);
  assert.equal(header.includes("/admin/editorial"), false);
});
