export const assistantName = "المساعد اللغوي";
export const assistantDescription = "اسأل عن كلمة، قاعدة، جذر، أسلوب بلاغي أو موضوع في اللغة العربية، وسيجيبك المساعد اعتمادًا على محتوى المرجع المنشور.";
export const assistantPromise = "إجابات موثقة من محتوى المرجع";
export const assistantDisabled = "المساعد الذكي غير متاح حاليًا. يمكنك استخدام البحث والأدوات اللغوية.";
export const assistantPlaceholder = "مثال: ما الفرق بين همزة الوصل وهمزة القطع؟";

export const groundingLabel: Record<string, string> = {
  GROUNDED: "موثّق من المرجع",
  PARTIALLY_GROUNDED: "إجابة جزئية من المرجع",
  INSUFFICIENT_EVIDENCE: "لا تتوفر معلومات كافية",
};

export type AssistantStatus = {
  available: boolean;
  message: string;
  suggestions: string[];
};

export type EvidenceCard = {
  evidenceId: string;
  title: string;
  typeLabel: string;
  excerpt: string;
  sourceLabel?: string | null;
  href: string;
  provenanceLabel: string;
};

export type AssistantAnswer = {
  requestId: string;
  answer: string;
  grounding: string;
  groundingLabel: string;
  grounded: boolean;
  uncertain: boolean;
  citations: { evidenceId: string; title: string; href: string; sourceLabel?: string | null }[];
  evidence: EvidenceCard[];
  limitations: string[];
  nearby: { title: string; href: string; note: string }[];
  tools: { label: string; href: string }[];
};

export function assistantApiBase(): string {
  return process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
}
