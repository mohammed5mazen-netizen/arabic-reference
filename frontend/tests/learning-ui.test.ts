import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";
import { visibleAdminNav } from "../lib/admin-nav.ts";
import { assistantLessonQuestion, durationLabel, learningSlug, lessonHref, passingLabel } from "../lib/learning.ts";
import { completeLesson, emptyProgress, progressPercent, readProgress, saveQuizScore } from "../lib/learning-progress.ts";
import { resultTypeLabel } from "../lib/search.ts";
import { sections, textDirection } from "../lib/site.ts";

test("learning routes labels and local progress stay anonymous", () => {
  assert.equal(textDirection, "rtl");
  assert.equal(lessonHref("النحو", "الفاعل"), "/learn/%D8%A7%D9%84%D9%86%D8%AD%D9%88/%D8%A7%D9%84%D9%81%D8%A7%D8%B9%D9%84");
  assert.equal(learningSlug("%D8%A7%D9%84%D9%86%D8%AD%D9%88"), "النحو");
  assert.equal(learningSlug("النحو"), "النحو");
  assert.match(decodeURIComponent(assistantLessonQuestion("الفاعل")), /اشرح لي درس/);
  assert.equal(passingLabel(true), "ناجح");
  assert.equal(passingLabel(false), "لم تبلغ درجة النجاح");
  assert.equal(durationLabel(12), "12 دقيقة");
  assert.equal(resultTypeLabel("LESSON"), "درس");
  assert.equal(resultTypeLabel("LEARNING_PATH"), "مسار تعليمي");
  assert.equal(sections.find((section) => section.id === "learning")?.href, "/learn");
  const store = memory();
  assert.deepEqual(readProgress(store), emptyProgress());
  completeLesson(store, "الفاعل");
  saveQuizScore(store, "quiz", 80, true);
  const saved = readProgress(store);
  assert.deepEqual(saved.completedLessonSlugs, ["الفاعل"]);
  assert.equal(saved.quizScores[0]?.score, 80);
  assert.equal(progressPercent(saved.completedLessonSlugs, ["الفاعل", "المفعول"]), 50);
  store.setItem("ar.learning.v1", "{\"schema\":0}");
  assert.deepEqual(readProgress(store).completedLessonSlugs, []);
});

test("learning pages keep answers off the server render and stay readable", () => {
  const home = readFileSync(new URL("../app/(public)/learn/page.tsx", import.meta.url), "utf8");
  const path = readFileSync(new URL("../app/(public)/learn/[pathSlug]/page.tsx", import.meta.url), "utf8");
  const lesson = readFileSync(new URL("../app/(public)/learn/[pathSlug]/[lessonSlug]/page.tsx", import.meta.url), "utf8");
  const quiz = readFileSync(new URL("../components/lesson-quiz.tsx", import.meta.url), "utf8");
  const admin = readFileSync(new URL("../app/admin/(desk)/learning/page.tsx", import.meta.url), "utf8");
  const header = readFileSync(new URL("../lib/navigation.ts", import.meta.url), "utf8");
  assert.match(home, /canonical/);
  assert.match(home, /index: true/);
  assert.match(path, /learningSlug/);
  assert.match(path, /difficultyLabel/);
  assert.match(lesson, /learningSlug/);
  assert.match(lesson, /objectives/);
  assert.match(lesson, /references/);
  assert.match(lesson, /lg:grid-cols/);
  assert.match(lesson, /lg:hidden/);
  assert.match(lesson, /اسأل المساعد عن هذا الدرس/);
  assert.equal(lesson.includes("correct"), false);
  assert.match(quiz, /type=\{multiple \? "checkbox" : "radio"\}/);
  assert.match(quiz, /aria-live="assertive"/);
  assert.match(quiz, /role="alert"/);
  assert.match(quiz, /حاول مرة أخرى/);
  assert.equal(quiz.includes("correct:"), false);
  assert.match(admin, /المسارات/);
  assert.match(admin, /الأسئلة/);
  assert.match(admin, /learning.lesson.publish/);
  assert.match(admin, /معاينة/);
  assert.match(header, /href: "\/learn"/);
  assert.equal(visibleAdminNav(["learning.lesson.view"]).some((item) => item.href === "/admin/learning"), true);
  assert.equal(visibleAdminNav(["admin.audit.view"]).some((item) => item.href === "/admin/learning"), false);
});

function memory() {
  const values = new Map<string, string>();
  return {
    getItem(key: string) {
      return values.get(key) ?? null;
    },
    setItem(key: string, value: string) {
      values.set(key, value);
    },
  };
}
