import type { Metadata } from "next";
import { resolveSiteUrl } from "./site.ts";

export type ToolStatus = "AVAILABLE" | "LIMITED";

export type ToolDefinition = {
  code: string;
  name: string;
  description: string;
  route: string;
  status: ToolStatus;
  statusLabel: string;
  inputKind: string;
  placeholder: string;
  endpoint: string;
  fields: "q" | "pair" | "morphology";
};

export const linguisticTools: ToolDefinition[] = [
  {
    code: "MORPHOLOGY",
    name: "المحلل الصرفي",
    description: "تحليل صرفي محدود من المحرك المنشور والقواعد المصرّح بها.",
    route: "/tools/morphology",
    status: "LIMITED",
    statusLabel: "تغطية محدودة",
    inputKind: "كلمة",
    placeholder: "اكتب كلمة عربية لتحليلها صرفيًا",
    endpoint: "/api/v1/public/morphology/analyze",
    fields: "morphology",
  },
  {
    code: "ROOT",
    name: "مستكشف الجذر",
    description: "يعرض الجذر الموثّق، أو جذرًا محتملًا إذا كان مستنتجًا بقاعدة.",
    route: "/tools/root",
    status: "AVAILABLE",
    statusLabel: "متاح",
    inputKind: "كلمة أو جذر",
    placeholder: "مثال: كتاب",
    endpoint: "/api/v1/public/tools/root",
    fields: "q",
  },
  {
    code: "DERIVATIONS",
    name: "مستكشف المشتقات",
    description: "شبكة المشتقات المنشورة فقط، بلا توليد كلمات جديدة.",
    route: "/tools/derivations",
    status: "AVAILABLE",
    statusLabel: "متاح",
    inputKind: "كلمة أو جذر",
    placeholder: "مثال: كتب",
    endpoint: "/api/v1/public/tools/derivations",
    fields: "q",
  },
  {
    code: "PATTERNS",
    name: "مستكشف الأوزان",
    description: "الأوزان المسجّلة وأمثلة منشورة مرتبطة بها.",
    route: "/tools/patterns",
    status: "AVAILABLE",
    statusLabel: "متاح",
    inputKind: "وزن أو كلمة",
    placeholder: "مثال: فاعل",
    endpoint: "/api/v1/public/tools/patterns",
    fields: "q",
  },
  {
    code: "WORD_ANALYSIS",
    name: "محلل الكلمة",
    description: "يجمع ما هو منشور عن كلمة واحدة، ولا يولّد معنى.",
    route: "/tools/word-analysis",
    status: "AVAILABLE",
    statusLabel: "متاح",
    inputKind: "كلمة واحدة",
    placeholder: "مثال: كتاب",
    endpoint: "/api/v1/public/tools/word-analysis",
    fields: "q",
  },
  {
    code: "COMPARE",
    name: "مقارنة الكلمات",
    description: "يعرض البيانات المنشورة لكلمتين جنبًا إلى جنب.",
    route: "/tools/compare",
    status: "AVAILABLE",
    statusLabel: "متاح",
    inputKind: "كلمتان",
    placeholder: "مثال: كتاب",
    endpoint: "/api/v1/public/tools/compare",
    fields: "pair",
  },
  {
    code: "RELATIONS",
    name: "المرادفات والأضداد",
    description: "علاقات كل معنى على حدة، بلا دمج المعاني.",
    route: "/tools/relations",
    status: "AVAILABLE",
    statusLabel: "متاح",
    inputKind: "كلمة",
    placeholder: "مثال: كتاب",
    endpoint: "/api/v1/public/tools/relations",
    fields: "q",
  },
  {
    code: "SPELLING_CHECK",
    name: "التحقق الإملائي المرجعي",
    description: "يطابق الصيغة مع المعجم والأخطاء الشائعة المسجّلة. ليس مدققًا آليًا.",
    route: "/tools/spelling-check",
    status: "LIMITED",
    statusLabel: "مرجعي",
    inputKind: "كلمة أو عبارة قصيرة",
    placeholder: "مثال: مسألة",
    endpoint: "/api/v1/public/tools/spelling-check",
    fields: "q",
  },
  {
    code: "GRAMMAR",
    name: "مستكشف القواعد النحوية",
    description: "يبحث في الموضوعات والقواعد المنشورة، ولا يعرب الجمل.",
    route: "/tools/grammar",
    status: "AVAILABLE",
    statusLabel: "متاح",
    inputKind: "مصطلح أو موضوع",
    placeholder: "مثال: فاعل",
    endpoint: "/api/v1/public/tools/grammar",
    fields: "q",
  },
  {
    code: "EXPLORE",
    name: "مستكشف العلاقات اللغوية",
    description: "رسم محدود للعلاقات المنشورة، مع قائمة نصية يمكن قراءتها.",
    route: "/tools/explore",
    status: "AVAILABLE",
    statusLabel: "متاح",
    inputKind: "كلمة",
    placeholder: "مثال: كتاب",
    endpoint: "/api/v1/public/tools/explore",
    fields: "q",
  },
];

export const featuredTools = ["ROOT", "WORD_ANALYSIS", "COMPARE", "SPELLING_CHECK"].map((code) => toolByCode(code));

export const toolGroups: { title: string; codes: string[] }[] = [
  { title: "تحليل", codes: ["MORPHOLOGY", "WORD_ANALYSIS", "ROOT"] },
  { title: "استكشاف", codes: ["DERIVATIONS", "PATTERNS", "EXPLORE"] },
  { title: "مقارنة", codes: ["COMPARE", "RELATIONS"] },
  { title: "كتابة", codes: ["SPELLING_CHECK", "GRAMMAR"] },
];

export function toolByCode(code: string): ToolDefinition {
  const tool = linguisticTools.find((item) => item.code === code);
  if (!tool) {
    throw new Error(`Unknown tool ${code}`);
  }
  return tool;
}

export function toolMetadata(tool: ToolDefinition, queried: boolean): Metadata {
  const canonical = `${resolveSiteUrl()}${tool.route}`;
  return {
    title: tool.name,
    description: tool.description,
    alternates: { canonical },
    robots: queried ? { index: false, follow: true } : { index: true, follow: true },
  };
}

export type ProvenanceNote = { kind: string; label: string };
export type SourceLink = { label: string; href: string };
export type EntryLink = { lemma: string; slug: string; partOfSpeechLabel?: string | null; href: string; note?: string | null };
export type SenseView = {
  definition: string;
  usageLabel?: string | null;
  domainLabel?: string | null;
  synonyms: EntryLink[];
  antonyms: EntryLink[];
  related: EntryLink[];
};
export type WordCard = {
  lemma: string;
  slug: string;
  href: string;
  partOfSpeechLabel?: string | null;
  root?: string | null;
  rootLabel?: string | null;
  rootHref?: string | null;
  senses: SenseView[];
  forms: string[];
  morphology: { pattern: string; categoryLabel: string; provenanceLabel: string }[];
  grammar: SourceLink[];
  spelling: SourceLink[];
  articles: SourceLink[];
  missing: string[];
};
export type ToolEnvelope<T> = {
  input: string;
  normalizedInput: string;
  tool: string;
  status: string;
  result: T;
  provenance: ProvenanceNote[];
  limitations: string[];
};
