export const siteName = "المرجع العربي";
export const siteTagline = "بوابتك الشاملة إلى اللغة العربية";
export const searchPlaceholder = "ابحث عن كلمة، معنى، جذر، قاعدة لغوية...";

export function resolveSiteUrl(): string {
  const site = process.env["SITE_URL"];
  const published = process.env["NEXT_PUBLIC_SITE_URL"];
  const configured = typeof site === "string" && site.trim() ? site : published;
  if (typeof configured === "string" && configured.trim()) {
    return configured.trim().replace(/\/$/, "");
  }
  return "http://localhost:3000";
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
    id: "tools",
    title: "الأدوات اللغوية",
    description: "أدوات تقرأ المعرفة المنشورة: الجذر، الصرف، المقارنة، والتحقق المرجعي.",
    href: "/tools",
    status: "متاح",
    action: "مركز الأدوات",
  },
] as const;
