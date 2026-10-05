export const siteName = "المرجع العربي";
export const siteTagline = "بوابتك الشاملة إلى اللغة العربية";
export const searchPlaceholder = "ابحث عن كلمة، معنى، جذر، قاعدة لغوية...";

export const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";
export const textDirection = "rtl";

export const sections = [
  {
    id: "dictionary",
    title: "المعجم",
    description: "الكلمات، المعاني، الجذور، والمشتقات في موضع واحد.",
    href: null,
    status: "قريبًا",
  },
  {
    id: "grammar",
    title: "النحو",
    description: "قواعد التركيب والإعراب، موثّقة بمصادرها.",
    href: "/grammar",
    status: "متاح",
  },
  {
    id: "morphology",
    title: "الصرف",
    description: "بنية الكلمة، الاشتقاق، وتصريف الأفعال.",
    href: "/tools/morphology",
    status: "متاح",
  },
  {
    id: "spelling",
    title: "الإملاء",
    description: "قواعد الكتابة العربية والهمزة والوصل.",
    href: "/spelling",
    status: "متاح",
  },
  {
    id: "rhetoric",
    title: "البلاغة",
    description: "البيان، المعاني، والبديع بأمثلة مضبوطة.",
    href: "/rhetoric",
    status: "متاح",
  },
  {
    id: "literature",
    title: "الأدب",
    description: "نصوص وسياقات من الأدب العربي.",
    href: "/literature",
    status: "متاح",
  },
  {
    id: "articles",
    title: "المقالات",
    description: "مقالات معرفية موثّقة عن العربية.",
    href: "/articles",
    status: "متاح",
  },
  {
    id: "sources",
    title: "المصادر",
    description: "الأصول، الطبعات، والاستشهاد الذي تقوم عليه المعلومة.",
    href: null,
    status: "قريبًا",
  },
  {
    id: "tools",
    title: "الأدوات اللغوية",
    description: "التحليل، التشكيل، والتدقيق عندما تكتمل المحركات.",
    href: null,
    status: "قريبًا",
  },
] as const;
