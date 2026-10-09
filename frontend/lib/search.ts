import { apiBase, partOfSpeechLabel } from "./dictionary.ts";

export const emptySearchMessage = "لم نعثر على نتائج مطابقة.";
export const unavailableSearchMessage = "تعذر إتمام البحث الآن.";
export const searchDebounceMs = 250;
export const suggestionLimit = 8;
export const searchFilterLayout = "scroll-row";

export type SearchHighlight = { field: string; start: number; end: number };
export type SearchHit = {
  type: string;
  id: string;
  title: string;
  subtitle?: string | null;
  snippet?: string | null;
  url: string;
  matchReason?: string | null;
  highlights: SearchHighlight[];
  metadata?: Record<string, string>;
};
export type SearchFacets = { dictionary: number; roots: number; grammar: number; content?: number };
export type SearchPage = {
  query: string;
  items: SearchHit[];
  page: number;
  size: number;
  total: number;
  facets: SearchFacets;
};
export type SearchSuggestion = { kind: string; title: string; url: string };

const reasonLabels: Record<string, string> = {
  EXACT: "مطابقة تامة",
  NORMALIZED_EXACT: "مطابق بعد التطبيع",
  WORD_FORM: "صيغة للكلمة",
  ALIAS: "اسم آخر",
  ROOT: "من الجذر",
  TITLE_PREFIX: "بداية الكلمة",
  MORPHOLOGY: "تطابق صرفي",
  DEFINITION: "تطابق في المعنى",
  FUZZY: "نتيجة قريبة",
};

export function matchReasonLabel(reason: string | null | undefined): string | null {
  if (!reason || reason === "EXACT") return null;
  return reasonLabels[reason] ?? null;
}

export function resultTypeLabel(type: string): string {
  switch (type) {
    case "DICTIONARY_ENTRY":
      return "مدخل معجمي";
    case "ROOT":
      return "جذر";
    case "GRAMMAR_RULE":
      return "قاعدة نحوية";
    case "GRAMMAR_TOPIC":
      return "موضوع";
    case "GRAMMAR_CONCEPT":
      return "مصطلح";
    case "SPELLING_RULE":
      return "قاعدة إملائية";
    case "SPELLING_TOPIC":
      return "موضوع إملائي";
    case "RHETORIC_DEVICE":
      return "فن بلاغي";
    case "RHETORIC_TOPIC":
      return "موضوع بلاغي";
    case "LITERARY_FIGURE":
      return "أديب";
    case "LITERARY_WORK":
      return "عمل أدبي";
    case "LITERARY_ERA":
      return "حقبة أدبية";
    case "ARTICLE":
      return "مقالة";
    case "LEARNING_PATH":
      return "مسار تعليمي";
    case "LESSON":
      return "درس";
    default:
      return type;
  }
}

export function searchPresentation(loaded: SearchPage | null, failed: boolean): "results" | "empty" | "unavailable" {
  if (failed) return "unavailable";
  if (!loaded || loaded.items.length === 0) return "empty";
  return "results";
}

export function searchHref(query: string, type = "all", page = 1): string {
  const params = new URLSearchParams();
  params.set("q", query.trim());
  if (type && type !== "all") params.set("type", type);
  if (page > 1) params.set("page", String(page));
  return `/search?${params.toString()}`;
}

export function suggestionUrl(query: string): string | null {
  const value = query.trim();
  if ([...value].length < 2) return null;
  return `${apiBase()}/api/v1/public/search/suggestions?q=${encodeURIComponent(value)}`;
}

export function suggestionIndex(current: number, key: string, count: number): number | "close" | "choose" {
  if (key === "Escape") return "close";
  if (key === "Enter") return "choose";
  if (count === 0) return current;
  if (key === "ArrowDown") return (current + 1) % count;
  if (key === "ArrowUp") return current <= 0 ? count - 1 : current - 1;
  return current;
}

export function highlightSegments(text: string, ranges: SearchHighlight[]): { text: string; highlighted: boolean }[] {
  const chars = Array.from(text);
  const marks = chars.map(() => false);
  for (const range of ranges) {
    for (let index = Math.max(0, range.start); index < Math.min(chars.length, range.end); index += 1) {
      marks[index] = true;
    }
  }
  const segments: { text: string; highlighted: boolean }[] = [];
  chars.forEach((char, index) => {
    const previous = segments[segments.length - 1];
    if (previous && previous.highlighted === marks[index]) previous.text += char;
    else segments.push({ text: char, highlighted: marks[index] });
  });
  return segments.length === 0 ? [{ text: "", highlighted: false }] : segments;
}

export function dictionaryCard(hit: SearchHit): { title: string; vocalized: string; speech: string; root: string; snippet: string } {
  return {
    title: hit.title,
    vocalized: hit.subtitle ?? "",
    speech: partOfSpeechLabel(hit.metadata?.partOfSpeech),
    root: hit.metadata?.root ? `الجذر: ${hit.metadata.root}` : "",
    snippet: hit.snippet ?? "",
  };
}

export function showsScore(hit: SearchHit): boolean {
  return Object.prototype.hasOwnProperty.call(hit, "score");
}

export function rebuildConfirmation(): string {
  return "يعيد هذا بناء فهرس البحث من المحتوى المنشور.";
}
