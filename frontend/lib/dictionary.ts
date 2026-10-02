export const emptyLookupMessage = "لم نعثر على مدخل منشور لهذه الكلمة.";
export const licenseWarning = "هذه الرخصة لا تسمح بالنشر العام. المحتوى لن يظهر للزوار.";

export type Attribution = {
  title?: string | null;
  author?: string | null;
  edition?: string | null;
  publicationYear?: number | null;
  pageFrom?: number | null;
  pageTo?: number | null;
};

export type PublicSense = {
  definition: string;
  shortDefinition?: string | null;
  usageLabel?: string | null;
  domainLabel?: string | null;
  displayOrder: number;
  examples: { textOriginal: string; explanation?: string | null; kind: string }[];
  sources: Attribution[];
};

export type PublicEntry = {
  id: string;
  slug: string;
  lemmaOriginal: string;
  vocalizedForm?: string | null;
  partOfSpeech: string;
  root?: { original: string; normalized: string; slug: string } | null;
  senses: PublicSense[];
  forms: { formType: string; originalForm: string }[];
  relations: { relationType: string; otherLemma: string; otherSlug: string; direction: string }[];
};

export type LookupHit = {
  id: string;
  slug: string;
  lemmaOriginal: string;
  vocalizedForm?: string | null;
  partOfSpeech: string;
  root?: string | null;
  shortDefinition?: string | null;
  senseCount: number;
};

export type LookupPage = {
  items: LookupHit[];
  page: number;
  size: number;
  total: number;
};

const partOfSpeechLabels: Record<string, string> = {
  NOUN: "اسم",
  VERB: "فعل",
  ADJECTIVE: "صفة",
  ADVERB: "ظرف",
  PRONOUN: "ضمير",
  PREPOSITION: "حرف جر",
  CONJUNCTION: "حرف عطف",
  PARTICLE: "أداة",
  INTERJECTION: "اسم فعل / تعجب",
  PROPER_NOUN: "علم",
  OTHER: "أخرى",
};

const relationLabels: Record<string, string> = {
  SYNONYM: "مرادف",
  ANTONYM: "ضد",
  RELATED: "علاقة",
  DERIVED_FROM: "مشتق",
};

export function canonicalSlug(value: string): string {
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

export function searchPath(query: string): string {
  return `/search?q=${encodeURIComponent(query.trim())}`;
}

export function partOfSpeechLabel(code: string | null | undefined): string {
  if (!code) return "";
  return partOfSpeechLabels[code] ?? code;
}

export function relationLabel(code: string): string {
  return relationLabels[code] ?? code;
}

export function wordTitle(lemma: string): string {
  return `${lemma} - المعنى والجذر`;
}

export function wordDescription(shortDefinition: string | null | undefined): string | undefined {
  const value = shortDefinition?.trim();
  return value ? value : undefined;
}

export function attributionLine(source: Attribution): string {
  const pages = source.pageFrom == null ? null : source.pageTo && source.pageTo !== source.pageFrom ? `ص ${source.pageFrom}–${source.pageTo}` : `ص ${source.pageFrom}`;
  return [source.title, source.author, source.edition, source.publicationYear, pages].filter((part) => part != null && String(part).length > 0).join(" · ");
}

export function licenseNeedsWarning(license: string): boolean {
  return license === "UNKNOWN" || license === "RESTRICTED";
}

export function apiBase(): string {
  return process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
}

export async function publicJson<T>(path: string): Promise<T | null> {
  const response = await fetch(`${apiBase()}${path}`, { cache: "no-store" });
  if (response.status === 404) return null;
  if (!response.ok) return null;
  const body = (await response.json()) as { data?: T };
  return body.data ?? null;
}
