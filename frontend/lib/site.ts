export const siteName = "المرجع العربي";
export const siteTagline = "بوابتك الشاملة إلى اللغة العربية";
export const searchPlaceholder = "ابحث عن كلمة، معنى، جذر، قاعدة لغوية...";

export const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";

export const sections = [
  {
    id: "dictionary",
    title: "المعجم",
    description: "الكلمات، المعاني، الجذور، والمشتقات في موضع واحد.",
  },
  {
    id: "grammar",
    title: "النحو",
    description: "قواعد التركيب والإعراب، موثّقة بمصادرها.",
  },
  {
    id: "morphology",
    title: "الصرف",
    description: "بنية الكلمة، الاشتقاق، وتصريف الأفعال.",
  },
  {
    id: "spelling",
    title: "الإملاء",
    description: "قواعد الكتابة العربية والهمزة والوصل.",
  },
  {
    id: "rhetoric",
    title: "البلاغة",
    description: "البيان، المعاني، والبديع بأمثلة مضبوطة.",
  },
  {
    id: "literature",
    title: "الأدب",
    description: "نصوص وسياقات من الأدب العربي.",
  },
  {
    id: "sources",
    title: "المصادر",
    description: "الأصول، الطبعات، والاستشهاد الذي تقوم عليه المعلومة.",
  },
  {
    id: "tools",
    title: "الأدوات اللغوية",
    description: "التحليل، التشكيل، والتدقيق عندما تكتمل المحركات.",
  },
] as const;
