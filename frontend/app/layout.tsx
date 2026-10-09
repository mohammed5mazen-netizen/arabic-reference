import type { Metadata } from "next";
import type { ReactNode } from "react";
import { connection } from "next/server";
import { Amiri, IBM_Plex_Sans_Arabic } from "next/font/google";
import { publicMetadata } from "@/lib/metadata";
import { indexingEnabled, resolveSiteUrl, siteName, textDirection } from "@/lib/site";
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

export async function generateMetadata(): Promise<Metadata> {
  await connection();
  const page = publicMetadata({ title: siteName, description: "بوابتك الشاملة إلى اللغة العربية", path: "/" });
  return {
    ...page,
    metadataBase: new URL(resolveSiteUrl()),
    title: {
      default: siteName,
      template: `%s | ${siteName}`,
    },
    robots: indexingEnabled() ? { index: true, follow: true } : { index: false, follow: false },
  };
}

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

export default async function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  await connection();

  return (
    <html lang="ar" dir={textDirection} className={`${amiri.variable} ${plex.variable}`} suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeScript }} />
      </head>
      <body>
        <a className="skip-link" href="#content">
          تجاوز إلى المحتوى
        </a>
        {children}
      </body>
    </html>
  );
}
