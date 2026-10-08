"use client";

import { useSyncExternalStore } from "react";
import { formatNumber } from "@/lib/format";
import { completeLesson, emptyProgress, progressPercent, readProgress, rememberLesson } from "@/lib/learning-progress";

function subscribe(listener: () => void) {
  window.addEventListener("ar-learning-progress", listener);
  return () => window.removeEventListener("ar-learning-progress", listener);
}

export function LearnStatus({ pathSlug, lessonSlug, title, lessonSlugs }: { pathSlug?: string; lessonSlug?: string; title?: string; lessonSlugs: string[] }) {
  const raw = useSyncExternalStore(subscribe, () => localStorage.getItem("ar.learning.v1") ?? "", () => "");
  const progress = raw ? readProgress({ getItem: () => raw, setItem() { return undefined; } }) : emptyProgress();
  const percent = progressPercent(progress.completedLessonSlugs, lessonSlugs);
  const done = lessonSlug ? progress.completedLessonSlugs.includes(lessonSlug) : false;

  return (
    <section className="mt-6 rounded-3xl border border-line bg-raised p-5" aria-labelledby="local-progress">
      <h2 id="local-progress" className="font-display text-2xl">تقدّمك على هذا الجهاز</h2>
      <p className="mt-2 text-sm leading-7 text-muted">يُحفظ هنا فقط، ولا يُرسل إلى الخادم. فتح الصفحة لا يعني إكمال الدرس.</p>
      {lessonSlugs.length ? (
        <p className="mt-3">
          <progress className="h-3 w-full" value={percent} max={100}>{formatNumber(percent)}٪</progress>
          <span className="mt-1 block text-sm">{formatNumber(progress.completedLessonSlugs.filter((slug) => lessonSlugs.includes(slug)).length)} من {formatNumber(lessonSlugs.length)} دروس · {formatNumber(percent)}٪</span>
        </p>
      ) : null}
      {progress.lastLesson ? <p className="mt-2 text-sm">آخر درس: {progress.lastLesson.title}</p> : null}
      {lessonSlug ? (
        <button
          type="button"
          className="mt-4 min-h-12 rounded-2xl bg-library px-4 text-white"
          onClick={() => {
            completeLesson(localStorage, lessonSlug);
            if (pathSlug && title) rememberLesson(localStorage, { pathSlug, lessonSlug, title });
          }}
        >
          {done ? "مكتمل" : "أكملت هذا الدرس"}
        </button>
      ) : null}
    </section>
  );
}
