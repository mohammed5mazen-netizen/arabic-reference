import type { MetadataRoute } from "next";
import { indexingEnabled, resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

export default function robots(): MetadataRoute.Robots {
  const origin = resolveSiteUrl();
  if (!indexingEnabled()) {
    return {
      rules: { userAgent: "*", disallow: "/" },
      sitemap: `${origin}/sitemap.xml`,
      host: origin,
    };
  }
  return {
    rules: {
      userAgent: "*",
      allow: "/",
      disallow: ["/admin/", "/api/", "/search"],
    },
    sitemap: `${origin}/sitemap.xml`,
    host: origin,
  };
}
