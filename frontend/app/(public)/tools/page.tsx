import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { linguisticTools } from "@/lib/tools";
import { resolveSiteUrl } from "@/lib/site";

export async function generateMetadata(): Promise<Metadata> {
  await connection();
  return {
    title: "الأدوات اللغوية",
    description: "أدوات تقرأ المعرفة المنشورة: الجذر، المشتقات، الأوزان، المقارنة، والتحقق الإملائي المرجعي.",
    alternates: { canonical: `${resolveSiteUrl()}/tools` },
  };
}

export default function ToolsPage() {
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-6xl px-5 py-10">
      <p className="text-sm text-library">مركز الأدوات</p>
      <h1 className="mt-2 font-display text-5xl sm:text-6xl">الأدوات اللغوية</h1>
      <p className="mt-4 max-w-3xl leading-8 text-muted">
        أدوات تقرأ المعرفة المنشورة في المرجع. لا تولّد معنى، ولا تعرب الجمل، ولا تدّعي تصحيحًا إملائيًا آليًا.
      </p>
      <ul className="mt-8 grid gap-4 sm:grid-cols-2">
        {linguisticTools.map((tool) => (
          <li key={tool.code}>
            <article className="flex h-full flex-col rounded-3xl border border-line bg-raised p-5">
              <div className="mb-4 flex items-center justify-between gap-3">
                <h2 className="font-display text-3xl">{tool.name}</h2>
                <span className="rounded-full bg-library-soft px-3 py-1 text-xs text-library">{tool.statusLabel}</span>
              </div>
              <p className="leading-7 text-muted">{tool.description}</p>
              <p className="mt-3 text-sm">المدخل: {tool.inputKind}</p>
              <Link href={tool.route} className="mt-4 text-library">
                استخدام الأداة
              </Link>
            </article>
          </li>
        ))}
      </ul>
    </main>
  );
}
