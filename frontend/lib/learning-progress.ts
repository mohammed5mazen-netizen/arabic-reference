export const learningProgressSchema = 1;
const storageKey = "ar.learning.v1";

export type LearningProgress = {
  schema: 1;
  completedLessonSlugs: string[];
  quizScores: { quizId: string; score: number; passed: boolean }[];
  lastLesson: { pathSlug: string; lessonSlug: string; title: string } | null;
};

export type ProgressStore = {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
};

export function emptyProgress(): LearningProgress {
  return { schema: learningProgressSchema, completedLessonSlugs: [], quizScores: [], lastLesson: null };
}

export function readProgress(store: ProgressStore): LearningProgress {
  const raw = store.getItem(storageKey);
  if (!raw) return emptyProgress();
  try {
    const parsed = JSON.parse(raw) as LearningProgress;
    if (parsed.schema !== learningProgressSchema || !Array.isArray(parsed.completedLessonSlugs)) return emptyProgress();
    return parsed;
  } catch {
    return emptyProgress();
  }
}

function write(store: ProgressStore, next: LearningProgress): LearningProgress {
  store.setItem(storageKey, JSON.stringify(next));
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("ar-learning-progress"));
  }
  return next;
}

export function rememberLesson(store: ProgressStore, lesson: { pathSlug: string; lessonSlug: string; title: string }): LearningProgress {
  const current = readProgress(store);
  const next = { ...current, lastLesson: lesson };
  return write(store, next);
}

export function completeLesson(store: ProgressStore, lessonSlug: string): LearningProgress {
  const current = readProgress(store);
  const completedLessonSlugs = current.completedLessonSlugs.includes(lessonSlug)
    ? current.completedLessonSlugs
    : [...current.completedLessonSlugs, lessonSlug];
  const next = { ...current, completedLessonSlugs };
  return write(store, next);
}

export function saveQuizScore(store: ProgressStore, quizId: string, score: number, passed: boolean): LearningProgress {
  const current = readProgress(store);
  const quizScores = [...current.quizScores.filter((item) => item.quizId !== quizId), { quizId, score, passed }];
  const next = { ...current, quizScores };
  return write(store, next);
}

export function progressPercent(completedLessonSlugs: readonly string[], lessonSlugs: readonly string[]): number {
  if (lessonSlugs.length === 0) return 0;
  const done = lessonSlugs.filter((slug) => completedLessonSlugs.includes(slug)).length;
  return Math.floor((done * 100) / lessonSlugs.length);
}
