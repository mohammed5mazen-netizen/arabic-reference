import { ToolScreen } from "@/components/tool-screen";
import { toolByCode, toolMetadata } from "@/lib/tools";

const tool = toolByCode("COMPARE");

export async function generateMetadata({ searchParams }: { searchParams: Promise<{ a?: string; b?: string }> }) {
  const params = await searchParams;
  return toolMetadata(tool, Boolean(params.a || params.b));
}

export default async function CompareToolPage({ searchParams }: { searchParams: Promise<{ a?: string; b?: string }> }) {
  const params = await searchParams;
  return <ToolScreen tool={tool} initialLeft={params.a ?? ""} initialRight={params.b ?? ""} />;
}
