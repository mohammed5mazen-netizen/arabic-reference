export type LearningPathCard = {
  title: string;
  slug: string;
  summary: string;
  difficultyLabel: string;
  estimatedMinutes?: number | null;
};

export type LearningLessonLink = {
  title: string;
  slug: string;
  summary: string;
  estimatedMinutes?: number | null;
};

export type LearningPath = LearningPathCard & {
  description: string;
  prerequisite?: { title: string; slug: string } | null;
  units: { title: string; summary: string; lessons: LearningLessonLink[] }[];
};

export type LearningSection = {
  typeLabel: string;
  heading: string;
  body: string;
  exampleLabel?: string | null;
};

export type LearningReference = {
  kindLabel: string;
  title: string;
  href?: string | null;
  note?: string | null;
  available: boolean;
};

export type LearningActivity = {
  typeLabel: string;
  title: string;
  instructions: string;
};

export type LearningQuizMeta = {
  id: string;
  title: string;
  passingScore: number;
  questionCount: number;
};

export type LearningLesson = {
  title: string;
  slug: string;
  summary: string;
  pathTitle: string;
  pathSlug: string;
  estimatedMinutes?: number | null;
  objectives: string[];
  sections: LearningSection[];
  references: LearningReference[];
  activities: LearningActivity[];
  previous?: { title: string; slug: string } | null;
  next?: { title: string; slug: string } | null;
  quiz?: LearningQuizMeta | null;
};

export type QuizOption = { id: string; label: string };
export type QuizQuestion = { id: string; prompt: string; type: string; options: QuizOption[] };
export type QuizStart = { token: string; title: string; passingScore: number; questions: QuizQuestion[] };
export type QuizResultQuestion = {
  questionId: string;
  accepted: boolean;
  explanation: string;
  referenceTitle?: string | null;
  referenceHref?: string | null;
};
export type QuizResult = { score: number; passed: boolean; questions: QuizResultQuestion[] };

export function lessonHref(pathSlug: string, lessonSlug: string): string {
  return `/learn/${encodeURIComponent(pathSlug)}/${encodeURIComponent(lessonSlug)}`;
}

export function learningSlug(value: string): string {
  let current = value;
  for (let attempt = 0; attempt < 2; attempt += 1) {
    try {
      const decoded = decodeURIComponent(current);
      if (decoded === current) return current;
      current = decoded;
    } catch {
      return current;
    }
  }
  return current;
}

export function assistantLessonQuestion(title: string): string {
  return `/assistant?q=${encodeURIComponent(`اشرح لي درس ${title}`)}`;
}

export function passingLabel(passed: boolean): string {
  return passed ? "ناجح" : "لم تبلغ درجة النجاح";
}

export function durationLabel(minutes?: number | null): string | null {
  return minutes == null ? null : `${minutes} دقيقة`;
}
