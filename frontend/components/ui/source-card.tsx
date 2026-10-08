export function SourceCard({ title, detail }: { title: string; detail?: string | null }) {
  return (
    <article className="rounded-2xl border border-line bg-raised p-4">
      <p className="type-label">المصدر</p>
      <p className="mt-1 leading-8">{title}</p>
      {detail ? <p className="mt-1 text-sm text-muted"><bdi dir="ltr">{detail}</bdi></p> : null}
    </article>
  );
}
