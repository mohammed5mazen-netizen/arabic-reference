import type { MetadataRoute } from "next";
import { resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: "*",
      allow: "/",
      disallow: ["/admin", "/admin/"],
    },
    sitemap: `${resolveSiteUrl()}/sitemap.xml`,
    host: resolveSiteUrl(),
  };
}
