import { MorphologyTool } from "@/components/morphology-tool";
import { toolByCode, toolMetadata } from "@/lib/tools";

export async function generateMetadata({ searchParams }: { searchParams: Promise<{ word?: string }> }) {
  const params = await searchParams;
  return toolMetadata(toolByCode("MORPHOLOGY"), Boolean(params.word));
}

export default async function MorphologyToolPage({ searchParams }: { searchParams: Promise<{ word?: string }> }) {
  const params = await searchParams;
  return <MorphologyTool initialWord={params.word ?? ""} />;
}
