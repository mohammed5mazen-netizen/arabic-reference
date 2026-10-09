import { attributionLine, canonicalSlug, type Attribution } from "./dictionary.ts";

export const missingAnnotationMessage = "لا يتوفر تحليل نحوي موثق لهذه الجملة.";
export const editorialExampleNote = "مثال تحريري، وليس شاهدًا من مصدر.";

export const ruleEditorSections = [
  { id: "basics", label: "المعلومات الأساسية" },
  { id: "statement", label: "القاعدة" },
  { id: "conditions", label: "الشروط والاستثناءات" },
  { id: "examples", label: "الأمثلة" },
  { id: "relations", label: "العلاقات" },
  { id: "sources", label: "المصادر" },
  { id: "review", label: "المراجعة" },
] as const;

export const grammarAdminLinks = [
  { href: "/admin/grammar", label: "الموضوعات" },
  { href: "/admin/grammar/rules", label: "القواعد" },
  { href: "/admin/grammar/concepts", label: "المصطلحات" },
  { href: "/admin/grammar/examples", label: "الأمثلة" },
  { href: "/admin/grammar/annotations", label: "تحليل الجمل" },
  { href: "/admin/grammar/review", label: "المراجعات" },
] as const;

export const annotationEditorFields = ["surface", "position", "role", "state", "explanation"] as const;

const labels: Record<string, string> = {
  FOUNDATIONS: "أسس النحو",
  NOMINAL_SENTENCE: "الجملة الاسمية",
  VERBAL_SENTENCE: "الجملة الفعلية",
  MARFUAT: "المرفوعات",
  MANSUBAT: "المنصوبات",
  MAJRURAT: "المجرورات",
  TAWABI: "التوابع",
  NAWASIKH: "النواسخ",
  ASALIB: "الأساليب",
  NUMERALS: "العدد",
  OTHER: "أخرى",
  BEGINNER: "مبتدئ",
  INTERMEDIATE: "متوسط",
  ADVANCED: "متقدم",
  DEFINITION: "التعريف",
  CORE_RULE: "القاعدة",
  CONDITION: "الشرط",
  EXCEPTION: "الاستثناء",
  NOTE: "ملاحظة",
  WARNING: "تنبيه",
  TERMINOLOGY: "مصطلح",
  DIFFERENCE: "فرق",
  SCHOLARLY_NOTE: "فائدة",
  CONSTRUCTED: "مثال تحريري",
  QUOTED: "مثال مقتبس",
  QURANIC: "شاهد قرآني",
  POETRY: "شاهد شعري",
  PROSE: "شاهد نثري",
  COUNTEREXAMPLE: "مثال مقابل",
  RELATED_TO: "ذات صلة",
  PREREQUISITE_OF: "تمهيد ل",
  EXCEPTION_TO: "استثناء من",
  SPECIAL_CASE_OF: "حالة خاصة من",
  CONTRASTS_WITH: "يقابل",
  SEE_ALSO: "انظر أيضًا",
  TOPIC: "موضوع",
  RULE: "قاعدة",
  CONCEPT: "مصطلح",
  ANNOTATION: "تحليل جملة",
  DRAFT: "مسودة",
  IN_REVIEW: "قيد المراجعة",
  VERIFIED: "تم التحقق",
  PUBLISHED: "منشور",
  CHANGES_REQUESTED: "مطلوب تعديل",
  ARCHIVED: "مؤرشف",
};

export type GrammarCrumb = { label: string; href?: string };

export type GrammarToken = {
  surface: string;
  position: number;
  roleLabel?: string | null;
  stateLabel?: string | null;
  explanation?: string | null;
  lexical?: { lemma: string; slug: string } | null;
  morphology?: { patternOriginal?: string | null; label?: string | null } | null;
};

export type GrammarExample = {
  textOriginal: string;
  explanation?: string | null;
  exampleType: string;
  exampleTypeLabel?: string | null;
  editorial?: boolean;
  editorialNote?: string | null;
  surah?: number | null;
  ayah?: number | null;
  poet?: string | null;
  workTitle?: string | null;
  verseLocator?: string | null;
  source?: Attribution | null;
  tokens?: GrammarToken[];
  annotationNote?: string | null;
};

export type ExampleCard = { surface: string; lines: string[] };

export function grammarLabel(code: string | null | undefined): string {
  if (!code) return "";
  return labels[code] ?? code;
}

export function grammarTitle(kind: "topic" | "rule" | "concept", name: string): string {
  if (kind === "rule") return `${name} — القاعدة والأمثلة`;
  if (kind === "concept") return `${name} — المصطلح النحوي`;
  return `${name} — النحو`;
}

export function grammarSearchPath(query: string): string {
  return `/grammar?q=${encodeURIComponent(query.trim())}`;
}

export function grammarCrumbs(trail: GrammarCrumb[]): GrammarCrumb[] {
  return [{ label: "الرئيسية", href: "/" }, { label: "النحو", href: "/grammar" }, ...trail.map((item) => ({ ...item, label: item.label }))];
}

export function breadcrumbJsonLd(items: GrammarCrumb[], origin: string): Record<string, unknown> {
  return {
    "@context": "https://schema.org",
    "@type": "BreadcrumbList",
    itemListElement: items.map((item, index) => ({
      "@type": "ListItem",
      position: index + 1,
      name: item.label,
      ...(item.href ? { item: `${origin}${item.href}` } : {}),
    })),
  };
}

export function grammarWorkflowActions(status: string, permissions: readonly string[]): Array<"submit" | "verify" | "request-changes" | "publish" | "archive"> {
  const actions: Array<"submit" | "verify" | "request-changes" | "publish" | "archive"> = [];
  if ((status === "DRAFT" || status === "CHANGES_REQUESTED") && permissions.includes("grammar.rule.submit")) actions.push("submit");
  if (status === "IN_REVIEW" && permissions.includes("grammar.rule.review")) actions.push("verify", "request-changes");
  if (status === "VERIFIED" && permissions.includes("grammar.rule.publish")) actions.push("publish");
  if (status === "PUBLISHED" && permissions.includes("grammar.rule.archive")) actions.push("archive");
  return actions;
}

export function exampleCards(example: GrammarExample): { layout: "cards"; cards: ExampleCard[]; note?: string } {
  const tokens = [...(example.tokens ?? [])].sort((left, right) => left.position - right.position);
  if (tokens.length === 0) {
    return { layout: "cards", cards: [], note: example.annotationNote || undefined };
  }
  return {
    layout: "cards",
    cards: tokens.map((token) => ({
      surface: token.surface,
      lines: [token.roleLabel, token.stateLabel, token.explanation, token.lexical ? token.lexical.lemma : null, token.morphology?.label].filter((line): line is string => Boolean(line)),
    })),
  };
}

export function grammarAttribution(source: Attribution | null | undefined): string {
  if (!source) return "";
  return attributionLine(source);
}

export function grammarSlug(value: string): string {
  return canonicalSlug(value);
}
