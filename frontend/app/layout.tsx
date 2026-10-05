import type { Metadata } from "next";
import type { ReactNode } from "react";
import { Amiri, IBM_Plex_Sans_Arabic } from "next/font/google";
import { siteName, siteTagline, siteUrl, textDirection } from "@/lib/site";
import "./globals.css";

const amiri = Amiri({
  subsets: ["arabic", "latin"],
  weight: ["400", "700"],
  variable: "--font-amiri",
  display: "swap",
});

const plex = IBM_Plex_Sans_Arabic({
  subsets: ["arabic"],
  weight: ["400", "500", "600"],
  variable: "--font-plex",
  display: "swap",
});

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: {
    default: siteName,
    template: `%s | ${siteName}`,
  },
  description: siteTagline,
  alternates: {
    canonical: "/",
  },
  openGraph: {
    title: siteName,
    description: siteTagline,
    locale: "ar",
    type: "website",
    url: "/",
  },
  robots: {
    index: true,
    follow: true,
  },
};

const themeScript = `
(function () {
  try {
    var stored = localStorage.getItem("arabic-reference-theme");
    var theme = stored === "light" || stored === "dark"
      ? stored
      : (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
    if (theme === "dark") document.documentElement.classList.add("dark");
    document.documentElement.style.colorScheme = theme;
  } catch (error) {}
})();
`;

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "WebSite",
    name: siteName,
    description: siteTagline,
    inLanguage: "ar",
    url: siteUrl,
  };

  return (
    <html lang="ar" dir={textDirection} className={`${amiri.variable} ${plex.variable}`} suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeScript }} />
      </head>
      <body>
        <a className="skip-link" href="#content">
          تخطي إلى المحتوى
        </a>
        {children}
        <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      </body>
    </html>
  );
}
