import type { Metadata } from "next";
import { resolveSiteUrl, siteName } from "./site.ts";

export function publicMetadata(input: { title: string; description: string; path: string; index?: boolean }): Metadata {
  const canonical = `${resolveSiteUrl()}${input.path}`;
  const index = input.index !== false;
  return {
    title: input.title,
    description: input.description,
    alternates: { canonical },
    openGraph: {
      title: input.title,
      description: input.description,
      url: canonical,
      locale: "ar",
      siteName,
      type: "website",
    },
    robots: { index, follow: true },
  };
}
