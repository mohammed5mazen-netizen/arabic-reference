import Link from "next/link";
import { sections } from "@/lib/site";

export function SectionGrid() {
  return (
    <section aria-labelledby="sections-title" className="mx-auto mt-16 w-full max-w-6xl px-5 pb-20">
      <div className="mb-8 flex items-end justify-between gap-4">
        <div>
          <p className="text-sm text-library">أقسام المرجع</p>
          <h2 id="sections-title" className="mt-2 font-display text-4xl">
            معرفة لغوية، لا لوحة إدارة
          </h2>
        </div>
      </div>
      <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {sections.map((section, index) => (
          <li key={section.id} id={section.id}>
            <article className="flex h-full flex-col rounded-3xl border border-line bg-raised p-5">
              <div className="mb-6 flex items-center justify-between">
                <span className="font-display text-2xl text-library">{String(index + 1).padStart(2, "0")}</span>
                <span className="rounded-full bg-library-soft px-3 py-1 text-xs font-medium text-library">
                  {section.status}
                </span>
              </div>
              <h3 className="font-display text-3xl">{section.title}</h3>
              <p className="mt-3 text-sm leading-7 text-muted">{section.description}</p>
              {section.href ? (
                <Link href={section.href} className="mt-4 text-library">
                  {section.action}
                </Link>
              ) : null}
            </article>
          </li>
        ))}
      </ul>
    </section>
  );
}
