import type { MetadataRoute } from "next";
import { resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

export default function sitemap(): MetadataRoute.Sitemap {
  return [
    {
      url: resolveSiteUrl(),
      changeFrequency: "weekly",
      priority: 1,
    },
  ];
}
