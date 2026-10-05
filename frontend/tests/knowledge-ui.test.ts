import assert from "node:assert/strict";
import test from "node:test";
import { visibleAdminNav } from "../lib/admin-nav.ts";
import { sections, textDirection } from "../lib/site.ts";
import { searchHref, resultTypeLabel } from "../lib/search.ts";
import {
  articlePath,
  emptyArticlesMessage,
  emptyLiteratureMessage,
  emptySpellingMessage,
  emptyWorksMessage,
  excerptBlockedMessage,
  knowledgeCrumbs,
  knowledgeDirection,
  knowledgeWorkflowActions,
  literaryRoleLabel,
  literatureAdminSections,
  literatureWorkPath,
  poetryClass,
  rhetoricAdminSections,
  rightsAllowExcerpt,
  rightsLabel,
  showsFullTextButton,
  spellingAdminSections,
  spellingRulePath,
  articleAdminSections,
} from "../lib/knowledge.ts";

test("knowledge sections are public Arabic pages", () => {
  assert.equal(textDirection, "rtl");
  assert.equal(knowledgeDirection(), "rtl");
  assert.equal(sections.find((section) => section.id === "spelling")?.status, "متاح");
  assert.equal(sections.find((section) => section.id === "rhetoric")?.href, "/rhetoric");
  assert.equal(sections.find((section) => section.id === "literature")?.status, "متاح");
  assert.equal(sections.find((section) => section.id === "articles")?.href, "/articles");
  assert.equal(spellingRulePath("همزة"), "/spelling/rules/همزة");
  assert.equal(literatureWorkPath("ديوان"), "/literature/works/ديوان");
  assert.equal(articlePath("مقالة"), "/articles/مقالة");
});

test("breadcrumbs sources and empty states stay explicit", () => {
  const crumbs = knowledgeCrumbs("الإملاء", "/spelling", [{ label: "الهمزة" }]);
  assert.deepEqual(crumbs.map((item) => item.label), ["الرئيسية", "الإملاء", "الهمزة"]);
  assert.equal(emptySpellingMessage.includes("لا توجد"), true);
  assert.equal(emptyLiteratureMessage.includes("لا توجد"), true);
  assert.equal(emptyWorksMessage.includes("لا توجد أعمال"), true);
  assert.equal(emptyArticlesMessage.includes("لا توجد"), true);
  assert.equal(poetryClass.includes("whitespace-pre-wrap"), true);
});

test("rights block excerpts and never offer a full book", () => {
  assert.equal(rightsAllowExcerpt("PUBLIC_DOMAIN"), true);
  assert.equal(rightsAllowExcerpt("LICENSED"), true);
  assert.equal(rightsAllowExcerpt("RESTRICTED"), false);
  assert.equal(rightsAllowExcerpt("UNKNOWN"), false);
  assert.equal(rightsLabel("UNKNOWN"), "الحقوق غير محسومة");
  assert.equal(excerptBlockedMessage.includes("غير معروفة"), true);
  assert.equal(showsFullTextButton(), false);
  assert.equal(literaryRoleLabel("POET"), "شاعر");
});

test("admin knowledge navigation follows permissions", () => {
  assert.equal(visibleAdminNav(["spelling.rule.review"]).some((item) => item.href === "/admin/spelling"), true);
  assert.equal(visibleAdminNav(["dictionary.entry.view"]).some((item) => item.href === "/admin/rhetoric"), false);
  assert.equal(visibleAdminNav(["literature.view"]).some((item) => item.href === "/admin/literature"), true);
  assert.equal(visibleAdminNav(["content.article.publish"]).some((item) => item.href === "/admin/articles"), true);
  assert.deepEqual(spellingAdminSections, ["الموضوعات", "القواعد", "الأمثلة", "المراجعات"]);
  assert.deepEqual(rhetoricAdminSections.includes("الصلات"), true);
  assert.deepEqual(literatureAdminSections.includes("الحقوق"), true);
  assert.deepEqual(articleAdminSections.includes("المعرفة المرتبطة"), true);
  assert.deepEqual(knowledgeWorkflowActions("IN_REVIEW", ["spelling.rule.review"], "spelling.rule.edit", "spelling.rule.review", "spelling.rule.publish"), ["verify", "request-changes"]);
  assert.deepEqual(knowledgeWorkflowActions("VERIFIED", ["content.article.publish"], "content.article.edit", "content.article.review", "content.article.publish"), ["publish"]);
  assert.deepEqual(knowledgeWorkflowActions("VERIFIED", ["content.article.review"], "content.article.edit", "content.article.review", "content.article.publish"), ["request-changes"]);
  assert.deepEqual(knowledgeWorkflowActions("DRAFT", ["literature.view"], "literature.figure.manage", "literature.review", "literature.publish"), []);
});

test("search can group the new knowledge under content", () => {
  assert.equal(searchHref("تشبيه", "content", 2), "/search?q=%D8%AA%D8%B4%D8%A8%D9%8A%D9%87&type=content&page=2");
  assert.equal(resultTypeLabel("SPELLING_RULE"), "قاعدة إملائية");
  assert.equal(resultTypeLabel("RHETORIC_DEVICE"), "فن بلاغي");
  assert.equal(resultTypeLabel("LITERARY_FIGURE"), "أديب");
  assert.equal(resultTypeLabel("LITERARY_WORK"), "عمل أدبي");
  assert.equal(resultTypeLabel("ARTICLE"), "مقالة");
});
