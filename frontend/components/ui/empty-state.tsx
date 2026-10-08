import type { ReactNode } from "react";

export function EmptyState({ title, description, action }: { title: string; description?: string; action?: ReactNode }) {
  return (
    <div className="rounded-[var(--radius)] border border-line bg-raised p-6">
      <h2 className="font-display text-3xl">{title}</h2>
      {description ? <p className="mt-3 leading-8 text-muted">{description}</p> : null}
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}
