import { apiBase } from "./dictionary.ts";
import { sitemapPathAllowed } from "./seo.ts";

export type DiscoveryItem = { type: string; path: string; publishedAt?: string };

export async function loadDiscovery(page: number, size: number): Promise<{ items: DiscoveryItem[]; total: number }> {
  try {
    const response = await fetch(`${apiBase()}/api/v1/public/discovery?page=${page}&size=${size}`, { cache: "no-store" });
    if (!response.ok) return { items: [], total: 0 };
    const body = (await response.json()) as { data?: { items?: DiscoveryItem[]; total?: number } };
    const items = (body.data?.items ?? []).filter((item) => sitemapPathAllowed(item.path));
    return { items, total: body.data?.total ?? items.length };
  } catch {
    return { items: [], total: 0 };
  }
}
