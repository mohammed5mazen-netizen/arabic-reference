import { ToolScreen } from "@/components/tool-screen";
import { toolByCode, toolMetadata } from "@/lib/tools";

const tool = toolByCode("SPELLING_CHECK");

export async function generateMetadata({ searchParams }: { searchParams: Promise<{ q?: string }> }) {
  const params = await searchParams;
  return toolMetadata(tool, Boolean(params.q));
}

export default async function SpellingCheckPage({ searchParams }: { searchParams: Promise<{ q?: string }> }) {
  const params = await searchParams;
  return <ToolScreen tool={tool} initialQuery={params.q ?? ""} />;
}
