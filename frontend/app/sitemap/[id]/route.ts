import { loadDiscovery } from "@/lib/discovery";
import { corePublicPaths } from "@/lib/seo";
import { absoluteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

const CHUNK = 5000;
const PAGE = 500;

function escapeXml(value: string): string {
  return value.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
}

function urlEntry(location: string, lastModified?: string): string {
  const modified = lastModified ? `\n<lastmod>${escapeXml(lastModified)}</lastmod>` : "";
  return `<url>\n<loc>${escapeXml(location)}</loc>${modified}\n</url>`;
}

export async function GET(_request: Request, context: { params: Promise<{ id: string }> }) {
  const raw = (await context.params).id.replace(/\.xml$/, "");
  const entries: string[] = [];
  if (raw === "core") {
    for (const path of corePublicPaths) entries.push(urlEntry(absoluteUrl(path)));
  } else if (raw.startsWith("knowledge-")) {
    const chunk = Number(raw.slice("knowledge-".length));
    if (Number.isInteger(chunk) && chunk >= 0 && chunk < 10) {
      const pagesPerChunk = CHUNK / PAGE;
      for (let index = 0; index < pagesPerChunk; index += 1) {
        const page = await loadDiscovery(chunk * pagesPerChunk + index, PAGE);
        for (const item of page.items) entries.push(urlEntry(absoluteUrl(item.path), item.publishedAt));
        if (page.items.length < PAGE) break;
      }
    }
  } else {
    return new Response("Not found", { status: 404 });
  }
  const body = [
    `<?xml version="1.0" encoding="UTF-8"?>`,
    `<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">`,
    ...entries,
    `</urlset>`,
  ].join("\n");
  return new Response(body, {
    headers: {
      "Content-Type": "application/xml; charset=utf-8",
      "Cache-Control": "public, max-age=0, must-revalidate",
    },
  });
}
