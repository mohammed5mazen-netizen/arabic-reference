import type { ReactNode } from "react";

export function Card({ children, className = "" }: { children: ReactNode; className?: string }) {
  return <article className={`min-w-0 rounded-[var(--radius)] border border-line bg-raised p-5 ${className}`}>{children}</article>;
}
