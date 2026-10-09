import { loadDiscovery } from "@/lib/discovery";
import { absoluteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

const CHUNK = 5000;

function escapeXml(value: string): string {
  return value.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
}

export async function GET() {
  const first = await loadDiscovery(0, 1);
  const chunks = Math.min(10, Math.max(1, Math.ceil(first.total / CHUNK)));
  const locations = [absoluteUrl("/sitemap/core.xml")];
  for (let index = 0; index < chunks; index += 1) {
    locations.push(absoluteUrl(`/sitemap/knowledge-${index}.xml`));
  }
  const body = [
    `<?xml version="1.0" encoding="UTF-8"?>`,
    `<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">`,
    ...locations.map((location) => `<sitemap><loc>${escapeXml(location)}</loc></sitemap>`),
    `</sitemapindex>`,
  ].join("\n");
  return new Response(body, {
    headers: {
      "Content-Type": "application/xml; charset=utf-8",
      "Cache-Control": "public, max-age=0, must-revalidate",
    },
  });
}
