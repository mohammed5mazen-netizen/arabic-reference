const labels = {
  documented: "موثق",
  published: "منشور",
  possible: "تحليل محتمل",
  near: "نتيجة قريبة",
  example: "مثال تعليمي",
} as const;

export type ProvenanceKind = keyof typeof labels;

export function ProvenanceBadge({ kind }: { kind: ProvenanceKind }) {
  return (
    <span className="inline-flex min-h-8 items-center rounded-full border border-line bg-library-soft px-3 text-sm text-library">
      {labels[kind]}
    </span>
  );
}
