import type { Metadata } from "next";
import { absoluteUrl, canonicalPath, indexingEnabled, siteName } from "./site.ts";
import { describe } from "./seo.ts";

export function publicMetadata(input: {
  title: string;
  description?: string | null;
  path: string;
  index?: boolean;
  type?: "website" | "article";
}): Metadata {
  const path = canonicalPath(input.path);
  const canonical = absoluteUrl(path);
  const index = input.index !== false && indexingEnabled();
  const description = describe(input.description, input.title);
  const verification = process.env.GOOGLE_SITE_VERIFICATION?.trim();
  return {
    title: input.title,
    description,
    alternates: { canonical },
    openGraph: {
      title: input.title,
      description,
      url: canonical,
      locale: "ar",
      siteName,
      type: input.type ?? "website",
    },
    robots: { index, follow: index },
    ...(verification ? { verification: { google: verification } } : {}),
  };
}
