import type { Metadata } from "next";
import { connection } from "next/server";
import { AssistantPanel } from "@/components/assistant-panel";
import { assistantDescription, assistantName, assistantPromise } from "@/lib/assistant";
import { resolveSiteUrl } from "@/lib/site";

export async function generateMetadata(): Promise<Metadata> {
  await connection();
  return {
    title: assistantName,
    description: assistantDescription,
    alternates: { canonical: `${resolveSiteUrl()}/assistant` },
    robots: { index: true, follow: true },
  };
}

export default function AssistantPage() {
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <p className="text-sm text-library">{assistantPromise}</p>
      <h1 className="mt-2 font-display text-5xl sm:text-6xl">{assistantName}</h1>
      <p className="mt-4 leading-8 text-muted">{assistantDescription}</p>
      <AssistantPanel />
    </main>
  );
}
