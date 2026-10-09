export const siteName = "المرجع العربي";
export const siteTagline = "بوابتك الشاملة إلى اللغة العربية";
export const searchPlaceholder = "ابحث عن كلمة، معنى، جذر، قاعدة لغوية...";

export class SiteOriginError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "SiteOriginError";
  }
}

export function indexingEnabled(): boolean {
  return process.env.SEO_INDEXING_ENABLED === "true";
}

export function normalizeSiteUrl(raw: string): string {
  let parsed: URL;
  try {
    parsed = new URL(raw.trim());
  } catch {
    throw new SiteOriginError("SITE_URL must be an absolute http or https URL.");
  }
  if (parsed.protocol !== "http:" && parsed.protocol !== "https:") {
    throw new SiteOriginError("SITE_URL must use http or https.");
  }
  if (parsed.username || parsed.password || parsed.search || parsed.hash) {
    throw new SiteOriginError("SITE_URL must not include credentials, a query, or a fragment.");
  }
  if (parsed.pathname !== "/" && parsed.pathname !== "") {
    throw new SiteOriginError("SITE_URL must not include a path.");
  }
  return parsed.origin;
}

export function indexableOrigin(origin: string): boolean {
  const parsed = new URL(origin);
  const host = parsed.hostname.toLowerCase();
  return parsed.protocol === "https:" && host !== "localhost" && host !== "127.0.0.1" && host !== "::1" && !host.endsWith(".local");
}

export function resolveSiteUrl(): string {
  const preferred = process.env.SITE_URL?.trim();
  const fallback = process.env.NEXT_PUBLIC_SITE_URL?.trim();
  const vercelProductionUrl = process.env.VERCEL_PROJECT_PRODUCTION_URL?.trim();
  const configured = preferred || fallback || (vercelProductionUrl ? `https://${vercelProductionUrl}` : "");
  if (configured) {
    const origin = normalizeSiteUrl(configured);
    if (indexingEnabled() && !indexableOrigin(origin)) {
      throw new SiteOriginError("SEO indexing requires an https SITE_URL that is not a local host.");
    }
    return origin;
  }
  const building = process.env.NEXT_PHASE === "phase-production-build";
  if (process.env.NODE_ENV !== "production" || building) {
    return "http://localhost:3000";
  }
  throw new SiteOriginError("SITE_URL is required in production.");
}

export function absoluteUrl(path: string): string {
  return `${resolveSiteUrl()}${canonicalPath(path)}`;
}

export function canonicalPath(path: string): string {
  const bare = path.split("?")[0]?.split("#")[0] ?? "/";
  const collapsed = bare.replace(/\/{2,}/g, "/");
  if (collapsed === "" || collapsed === "/") return "/";
  const segments = collapsed.split("/").filter((segment) => segment.length > 0).map((segment) => encodeURIComponent(decodePathSegment(segment)));
  return `/${segments.join("/")}`;
}

export function decodePathSegment(value: string): string {
  let current = value;
  for (let attempt = 0; attempt < 2; attempt += 1) {
    try {
      const decoded = decodeURIComponent(current);
      if (decoded === current) return current;
      current = decoded;
    } catch {
      return current;
    }
  }
  return current;
}
export const textDirection = "rtl";

export const sections = [
  {
    id: "dictionary",
    title: "المعجم",
    description: "الكلمات، المعاني، الجذور، والمشتقات في موضع واحد.",
    href: null,
    status: "قريبًا",
    action: null,
  },
  {
    id: "grammar",
    title: "النحو",
    description: "قواعد التركيب والإعراب، موثّقة بمصادرها.",
    href: "/grammar",
    status: "متاح",
    action: "مرجع النحو",
  },
  {
    id: "morphology",
    title: "الصرف",
    description: "بنية الكلمة، الاشتقاق، وتصريف الأفعال.",
    href: "/tools/morphology",
    status: "متاح",
    action: "المحلل الصرفي",
  },
  {
    id: "spelling",
    title: "الإملاء",
    description: "قواعد الكتابة العربية والهمزة والوصل.",
    href: "/spelling",
    status: "متاح",
    action: "مرجع الإملاء",
  },
  {
    id: "rhetoric",
    title: "البلاغة",
    description: "البيان، المعاني، والبديع بأمثلة مضبوطة.",
    href: "/rhetoric",
    status: "متاح",
    action: "مرجع البلاغة",
  },
  {
    id: "literature",
    title: "الأدب",
    description: "نصوص وسياقات من الأدب العربي.",
    href: "/literature",
    status: "متاح",
    action: "مرجع الأدب",
  },
  {
    id: "articles",
    title: "المقالات",
    description: "مقالات معرفية موثّقة عن العربية.",
    href: "/articles",
    status: "متاح",
    action: "المقالات",
  },
  {
    id: "sources",
    title: "المصادر",
    description: "الأصول، الطبعات، والاستشهاد الذي تقوم عليه المعلومة.",
    href: null,
    status: "قريبًا",
    action: null,
  },
  {
    id: "learning",
    title: "التعلّم",
    description: "مسارات ودروس واختبارات مبنية على المعرفة المنشورة.",
    href: "/learn",
    status: "متاح",
    action: "ابدأ التعلّم",
  },
  {
    id: "tools",
    title: "الأدوات اللغوية",
    description: "أدوات تقرأ المعرفة المنشورة: الجذر، الصرف، المقارنة، والتحقق المرجعي.",
    href: "/tools",
    status: "متاح",
    action: "مركز الأدوات",
  },
] as const;
