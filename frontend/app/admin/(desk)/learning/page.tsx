"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";

type Session = { permissions: string[] };
type PathView = { id: string; version: number; status?: string; slug?: string; units?: { id: string; lessons?: { id: string; quiz?: { id: string } | null }[] }[] };

const sections = ["المسارات", "الوحدات", "الدروس", "الاختبارات", "الأسئلة", "المراجعات"];

export default function AdminLearningPage() {
  const [permissions, setPermissions] = useState<string[]>([]);
  const [title, setTitle] = useState("");
  const [pathId, setPathId] = useState("");
  const [unitId, setUnitId] = useState("");
  const [lessonId, setLessonId] = useState("");
  const [quizId, setQuizId] = useState("");
  const [questionId, setQuestionId] = useState("");
  const [version, setVersion] = useState(0);
  const [preview, setPreview] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<Session>("/api/v1/admin/auth/session").then((session) => {
      if (!cancelled) setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح التعلّم.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  function fail(caught: unknown) {
    setError(caught instanceof Error ? caught.message : "تعذر الحفظ.");
  }

  function apply(path: PathView) {
    setPathId(path.id);
    setVersion(path.version);
    setUnitId(path.units?.[0]?.id ?? unitId);
    setLessonId(path.units?.[0]?.lessons?.[0]?.id ?? lessonId);
    setQuizId(path.units?.[0]?.lessons?.[0]?.quiz?.id ?? quizId);
    setPreview(path.status ?? "");
  }

  return (
    <main id="content" className="space-y-8">
      <h1 className="font-display text-5xl">التعلّم</h1>
      <nav aria-label="أقسام التعلّم" className="flex flex-wrap gap-3">{sections.map((section) => <span key={section}>{section}</span>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      {preview ? <p>الحالة: {preview}</p> : null}
      {can(permissions, "learning.path.manage") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">المسارات</h2>
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="عنوان المسار" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void adminFetch<PathView>("/api/v1/admin/learning/paths", { method: "POST", body: JSON.stringify({ title, summary: title, description: title, difficulty: "BEGINNER", estimatedMinutes: 20, displayOrder: 1 }) }).then(apply).catch(fail)}>إنشاء مسار</button>
        </section>
      ) : null}
      {can(permissions, "learning.path.manage") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الوحدات</h2>
          <input value={pathId} onChange={(event) => setPathId(event.target.value)} placeholder="معرّف المسار" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/paths/${pathId}/units`, { method: "POST", body: JSON.stringify({ version, title, summary: title, displayOrder: 1 }) }).then(apply).catch(fail)}>إضافة وحدة</button>
        </section>
      ) : null}
      {can(permissions, "learning.lesson.create") || can(permissions, "learning.lesson.edit") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الدروس</h2>
          <input value={unitId} onChange={(event) => setUnitId(event.target.value)} placeholder="معرّف الوحدة" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <input value={lessonId} onChange={(event) => setLessonId(event.target.value)} placeholder="معرّف الدرس" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <div className="flex flex-wrap gap-3">
            {can(permissions, "learning.lesson.create") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/units/${unitId}/lessons`, { method: "POST", body: JSON.stringify({ version, title, summary: title, estimatedMinutes: 10, displayOrder: 1 }) }).then(apply).catch(fail)}>إنشاء درس</button> : null}
            {can(permissions, "learning.lesson.edit") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/lessons/${lessonId}/objectives`, { method: "POST", body: JSON.stringify({ version, text: title, displayOrder: 1 }) }).then(apply).catch(fail)}>هدف</button> : null}
            {can(permissions, "learning.lesson.edit") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/lessons/${lessonId}/sections`, { method: "POST", body: JSON.stringify({ version, type: "EXPLANATION", heading: title, body: title, exampleKind: "CONSTRUCTED", displayOrder: 1 }) }).then(apply).catch(fail)}>قسم</button> : null}
          </div>
        </section>
      ) : null}
      {can(permissions, "learning.quiz.manage") || can(permissions, "learning.question.manage") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الاختبارات والأسئلة</h2>
          <input value={quizId} onChange={(event) => setQuizId(event.target.value)} placeholder="معرّف الاختبار" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <input value={questionId} onChange={(event) => setQuestionId(event.target.value)} placeholder="معرّف السؤال" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <div className="flex flex-wrap gap-3">
            {can(permissions, "learning.quiz.manage") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/lessons/${lessonId}/quiz`, { method: "POST", body: JSON.stringify({ version, title, passingScore: 70 }) }).then(apply).catch(fail)}>اختبار</button> : null}
            {can(permissions, "learning.question.manage") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/quizzes/${quizId}/questions`, { method: "POST", body: JSON.stringify({ version, prompt: title, explanation: title, type: "TRUE_FALSE", difficulty: "BEGINNER", displayOrder: 1, options: [{ label: "صواب", correct: true }, { label: "خطأ", correct: false }] }) }).then(apply).catch(fail)}>سؤال</button> : null}
          </div>
        </section>
      ) : null}
      <section className="space-y-3">
        <h2 className="font-display text-3xl">المراجعات</h2>
        <div className="flex flex-wrap gap-3">
          {can(permissions, "learning.lesson.submit") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/paths/${pathId}/submit`, { method: "POST", body: JSON.stringify({ version }) }).then(apply).catch(fail)}>إرسال</button> : null}
          {can(permissions, "learning.lesson.review") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/paths/${pathId}/changes`, { method: "POST", body: JSON.stringify({ version }) }).then(apply).catch(fail)}>طلب تعديل</button> : null}
          {can(permissions, "learning.lesson.review") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/paths/${pathId}/verify`, { method: "POST", body: JSON.stringify({ version }) }).then(apply).catch(fail)}>تحقق</button> : null}
          {can(permissions, "learning.lesson.publish") ? <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/paths/${pathId}/publish`, { method: "POST", body: JSON.stringify({ version }) }).then(apply).catch(fail)}>نشر</button> : null}
          {can(permissions, "learning.lesson.archive") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/paths/${pathId}/archive`, { method: "POST", body: JSON.stringify({ version }) }).then(apply).catch(fail)}>أرشفة</button> : null}
          {can(permissions, "learning.lesson.view") ? <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<PathView>(`/api/v1/admin/learning/paths/${pathId}`).then((path) => { apply(path); setPreview(JSON.stringify(path)); }).catch(fail)}>معاينة</button> : null}
        </div>
      </section>
    </main>
  );
}
