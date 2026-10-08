import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { PageHeader } from "@/components/ui/page-header";
import { ProvenanceBadge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { publicMetadata } from "@/lib/metadata";
import { toolByCode, toolGroups } from "@/lib/tools";

export async function generateMetadata(): Promise<Metadata> {
  await connection();
  return publicMetadata({
    title: "الأدوات اللغوية",
    description: "أدوات تقرأ المعرفة المنشورة: الجذر، المشتقات، الأوزان، المقارنة، والتحقق الإملائي المرجعي.",
    path: "/tools",
  });
}

export default function ToolsPage() {
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-6xl px-5 py-10">
      <PageHeader eyebrow="مركز الأدوات" title="الأدوات اللغوية" summary="أدوات تقرأ المعرفة المنشورة في المرجع. لا تولّد معنى، ولا تعرب الجمل، ولا تدّعي تصحيحًا إملائيًا آليًا." />
      <div className="mt-10 space-y-10">
        {toolGroups.map((group) => (
          <section key={group.title} aria-labelledby={group.title}>
            <h2 id={group.title} className="font-display text-3xl">{group.title}</h2>
            <ul className="mt-4 grid gap-4 sm:grid-cols-2">
              {group.codes.map((code) => {
                const tool = toolByCode(code);
                return (
                  <li key={tool.code}>
                    <Card className="flex h-full flex-col">
                      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
                        <h3 className="font-display text-3xl">{tool.name}</h3>
                        <ProvenanceBadge kind={tool.code === "MORPHOLOGY" ? "possible" : tool.status === "LIMITED" ? "documented" : "published"} />
                      </div>
                      <p className="leading-7 text-muted">{tool.description}</p>
                      <p className="mt-3 text-sm">المدخل: {tool.inputKind}</p>
                      <Link href={tool.route} className="mt-4 inline-flex min-h-11 items-center text-library">استخدام الأداة</Link>
                    </Card>
                  </li>
                );
              })}
            </ul>
          </section>
        ))}
      </div>
    </main>
  );
}
