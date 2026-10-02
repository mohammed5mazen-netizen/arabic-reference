export type MorphFeatures = {
  person?: string | null;
  number?: string | null;
  gender?: string | null;
  aspect?: string | null;
  mood?: string | null;
  voice?: string | null;
  grammaticalCase?: string | null;
  definiteness?: string | null;
};

export type MorphCandidate = {
  surfaceForm: string;
  normalizedForm: string;
  lemma?: string | null;
  lexicalEntryId?: string | null;
  root?: string | null;
  patternCode?: string | null;
  patternOriginal?: string | null;
  patternCategory?: string | null;
  partOfSpeech?: string | null;
  features?: MorphFeatures | null;
  segmentation?: { clitics: string[]; prefixes: string[]; stem: string; suffixes: string[] } | null;
  derivation?: string | null;
  provenance: string;
  explanationCodes: string[];
};

export type AnalysisReport = {
  input: string;
  normalizedInput: string;
  analyses: MorphCandidate[];
  truncated: boolean;
  limit: number;
  ruleSetVersion: string;
  resultClass?: string | null;
};

export type EntryMorphology = {
  entryId: string;
  readings: {
    patternOriginal?: string | null;
    patternCategory?: string | null;
    derivation?: string | null;
    verbClass?: string | null;
    imperfectVowel?: string | null;
    notes?: string | null;
    features?: MorphFeatures | null;
  }[];
  recordedPlurals: string[];
};

export type RootMorphology = {
  root: string;
  slug: string;
  patterns: { code?: string | null; original?: string | null; lemma: string; entrySlug: string; derivation?: string | null }[];
};

const labels: Record<string, string> = {
  VERB: "فعل",
  ACTIVE_PARTICIPLE: "اسم فاعل",
  PASSIVE_PARTICIPLE: "اسم مفعول",
  VERBAL_NOUN: "مصدر",
  NOUN: "اسم",
  ADJECTIVE: "صفة",
  PLACE_NOUN: "اسم مكان",
  TIME_NOUN: "اسم زمان",
  INSTRUMENT_NOUN: "اسم آلة",
  OTHER: "أخرى",
  ROOT_DERIVED: "مشتق من الجذر",
  RELATIVE_ADJECTIVE: "نسبة",
  DIMINUTIVE: "تصغير",
  FIRST: "متكلم",
  SECOND: "مخاطب",
  THIRD: "غائب",
  SINGULAR: "مفرد",
  DUAL: "مثنى",
  PLURAL: "جمع",
  MASCULINE: "مذكر",
  FEMININE: "مؤنث",
  NOT_APPLICABLE: "غير منطبق",
  PERFECT: "ماضٍ",
  IMPERFECT: "مضارع",
  IMPERATIVE: "أمر",
  INDICATIVE: "مرفوع",
  SUBJUNCTIVE: "منصوب",
  JUSSIVE: "مجزوم",
  ACTIVE: "معلوم",
  PASSIVE: "مجهول",
  NOMINATIVE: "رفع",
  ACCUSATIVE: "نصب",
  GENITIVE: "جر",
  DEFINITE: "معرفة",
  INDEFINITE: "نكرة",
  CONSTRUCT_STATE: "إضافة",
  SOUND: "صحيح سالم",
  HAMZATED: "مهموز",
  ASSIMILATED: "مثال",
  HOLLOW: "أجوف",
  DEFECTIVE: "ناقص",
  DOUBLED: "مضعف",
  LAFIF_MAFRUQ: "لفيف مفروق",
  LAFIF_MAQRUN: "لفيف مقرون",
  FATHA: "فتحة",
  KASRA: "كسرة",
  DAMMA: "ضمة",
  MANUAL_VERIFIED: "موثق يدويًا",
  EXACT_DICTIONARY: "مطابق للمعجم",
  RULE_DERIVED: "مستنتج بقاعدة",
  AMBIGUOUS: "تحليل محتمل",
  SUPPORTED: "مدعوم",
  PARTIALLY_SUPPORTED: "مدعوم جزئيًا",
  UNSUPPORTED: "غير مدعوم",
  RULE_GENERATED: "مولَّد بقاعدة",
  DICTIONARY: "من المعجم",
  DRAFT: "مسودة",
  IN_REVIEW: "قيد المراجعة",
  VERIFIED: "تم التحقق",
  PUBLISHED: "منشور",
  CHANGES_REQUESTED: "مطلوب تعديل",
  ARCHIVED: "مؤرشف",
};

export function morphologyLabel(code: string | null | undefined): string {
  if (!code) return "";
  return labels[code] ?? code;
}

export function featureLines(features: MorphFeatures | null | undefined): string[] {
  if (!features) return [];
  const rows: [string, string | null | undefined][] = [
    ["الشخص", features.person],
    ["العدد", features.number],
    ["الجنس", features.gender],
    ["الباب الزمني", features.aspect],
    ["الحالة الإعرابية للفعل", features.mood],
    ["البناء للمعلوم", features.voice],
    ["الحالة", features.grammaticalCase],
    ["التعريف", features.definiteness],
  ];
  return rows.filter((row) => row[1]).map(([name, value]) => `${name}: ${morphologyLabel(value)}`);
}

export function segmentationLine(candidate: MorphCandidate): string {
  const parts = [
    ...(candidate.segmentation?.clitics ?? []),
    ...(candidate.segmentation?.prefixes ?? []),
    candidate.segmentation?.stem,
    ...(candidate.segmentation?.suffixes ?? []),
  ].filter((part) => part);
  return parts.join(" + ");
}
