import { apiBase, partOfSpeechLabel } from "./dictionary.ts";

export const emptySearchMessage = "لم نعثر على نتائج مطابقة.";
export const emptySearchPrompt = "ابدأ بكتابة كلمة أو جذر أو موضوع للبحث في المرجع.";
export const unavailableSearchMessage = "تعذر إكمال البحث. أعد المحاولة.";
export const searchDebounceMs = 250;
export const suggestionLimit = 8;
export const searchFilterLayout = "scroll-row";
export const searchEndpoint = "/api/v1/public/search";

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
export type SearchFacets = { dictionary: number; roots: number; grammar: number; content: number; learning?: number };
export type SearchPage = {
  query: string;
  items: SearchHit[];
  page: number;
  size: number;
  total: number;
  facets: SearchFacets;
};
export type SearchSuggestion = { kind: string; title: string; url: string };

type SearchOptions = {
  type?: string;
  page?: number;
  size?: number;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isCount(value: unknown): value is number {
  return typeof value === "number" && Number.isSafeInteger(value) && value >= 0;
}

function parseSearchPage(body: unknown): SearchPage {
  if (!isRecord(body) || !isRecord(body.data)) {
    throw new TypeError("Search response is missing its data object.");
  }
  const data = body.data;
  if (
    typeof data.query !== "string"
    || !Array.isArray(data.items)
    || typeof data.page !== "number"
    || !Number.isSafeInteger(data.page)
    || data.page < 1
    || typeof data.size !== "number"
    || !Number.isSafeInteger(data.size)
    || data.size < 1
    || !isCount(data.total)
    || !isRecord(data.facets)
    || !isCount(data.facets.dictionary)
    || !isCount(data.facets.roots)
    || !isCount(data.facets.grammar)
    || !isCount(data.facets.content)
    || (data.facets.learning !== undefined && !isCount(data.facets.learning))
  ) {
    throw new TypeError("Search response does not match the public search contract.");
  }
  const items = data.items.map((value): SearchHit => {
    if (!isRecord(value) || typeof value.type !== "string" || typeof value.id !== "string" || typeof value.title !== "string" || typeof value.url !== "string") {
      throw new TypeError("Search response contains an invalid result.");
    }
    const highlights = Array.isArray(value.highlights)
      ? value.highlights.flatMap((highlight): SearchHighlight[] => {
          if (
            !isRecord(highlight)
            || typeof highlight.field !== "string"
            || typeof highlight.start !== "number"
            || !Number.isSafeInteger(highlight.start)
            || typeof highlight.end !== "number"
            || !Number.isSafeInteger(highlight.end)
            || highlight.start < 0
            || highlight.end <= highlight.start
          ) {
            return [];
          }
          return [{ field: highlight.field, start: highlight.start, end: highlight.end }];
        })
      : [];
    const metadata = isRecord(value.metadata)
      ? Object.fromEntries(Object.entries(value.metadata).filter((entry): entry is [string, string] => typeof entry[1] === "string"))
      : {};
    return {
      type: value.type,
      id: value.id,
      title: value.title,
      url: value.url,
      subtitle: typeof value.subtitle === "string" ? value.subtitle : null,
      snippet: typeof value.snippet === "string" ? value.snippet : null,
      matchReason: typeof value.matchReason === "string" ? value.matchReason : null,
      highlights,
      metadata,
    };
  });
  return {
    query: data.query,
    items,
    page: data.page as number,
    size: data.size as number,
    total: data.total,
    facets: {
      dictionary: data.facets.dictionary,
      roots: data.facets.roots,
      grammar: data.facets.grammar,
      content: data.facets.content,
      ...(data.facets.learning === undefined ? {} : { learning: data.facets.learning }),
    },
  };
}

export function searchUrl(query: string, options: SearchOptions = {}): string | null {
  const q = query.trim();
  if (!q) return null;
  const params = new URLSearchParams({ q });
  if (options.type && options.type !== "all") params.set("type", options.type);
  params.set("page", String(Math.max(1, options.page ?? 1)));
  params.set("size", String(Math.max(1, options.size ?? 20)));
  return `${apiBase()}${searchEndpoint}?${params.toString()}`;
}

export function logSearchFailure(status: number | null, traceId?: string | null, endpoint = searchEndpoint): void {
  if (process.env.NODE_ENV !== "development") return;
  console.error("Public search request failed", {
    status,
    endpoint,
    ...(traceId ? { traceId } : {}),
  });
}

export async function searchPublic(query: string, options: SearchOptions = {}): Promise<SearchPage | null> {
  const url = searchUrl(query, options);
  if (!url) return null;
  try {
    const response = await fetch(url, { cache: "no-store" });
    let body: unknown;
    try {
      body = await response.json();
    } catch (error) {
      if (!response.ok) {
        logSearchFailure(response.status);
        throw new Error(`Public search request failed (${response.status}).`, { cause: error });
      }
      throw error;
    }
    if (!response.ok) {
      logSearchFailure(response.status, isRecord(body) && typeof body.traceId === "string" ? body.traceId : null);
      throw new Error(`Public search request failed (${response.status}).`);
    }
    return parseSearchPage(body);
  } catch (error) {
    if (!(error instanceof Error) || !error.message.startsWith("Public search request failed")) {
      logSearchFailure(null);
    }
    throw error;
  }
}

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
    if (!Number.isSafeInteger(range.start) || !Number.isSafeInteger(range.end) || range.end <= range.start) continue;
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
