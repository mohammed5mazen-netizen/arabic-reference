import Link from "next/link";
import { breadcrumbJsonLd, type GrammarCrumb } from "@/lib/grammar";
import { siteUrl } from "@/lib/site";

export function Breadcrumbs({ items }: { items: GrammarCrumb[] }) {
  return (
    <>
      <nav aria-label="مسار التنقل" className="mb-6">
        <ol className="flex flex-wrap items-center gap-2 text-sm text-muted">
          {items.map((item, index) => (
            <li key={`${item.label}-${index}`} className="flex items-center gap-2">
              {index > 0 ? <span aria-hidden="true">/</span> : null}
              {item.href && index < items.length - 1 ? (
                <Link href={item.href}>{item.label}</Link>
              ) : (
                <span aria-current={index === items.length - 1 ? "page" : undefined}>{item.label}</span>
              )}
            </li>
          ))}
        </ol>
      </nav>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(breadcrumbJsonLd(items, siteUrl)) }} />
    </>
  );
}
