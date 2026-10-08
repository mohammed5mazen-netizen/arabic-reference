export type NavLink = { href: string; label: string };

export const navigationGroups: { id: string; title: string; links: NavLink[] }[] = [
  {
    id: "language",
    title: "اللغة",
    links: [
      { href: "/search", label: "المعجم" },
      { href: "/grammar", label: "النحو" },
      { href: "/tools/morphology", label: "الصرف" },
      { href: "/spelling", label: "الإملاء" },
      { href: "/rhetoric", label: "البلاغة" },
    ],
  },
  {
    id: "knowledge",
    title: "المعرفة",
    links: [
      { href: "/literature", label: "الأدب" },
      { href: "/articles", label: "المقالات" },
    ],
  },
  {
    id: "services",
    title: "الخدمات",
    links: [
      { href: "/tools", label: "الأدوات" },
      { href: "/learn", label: "التعلّم" },
      { href: "/assistant", label: "المساعد" },
    ],
  },
];

export const knowledgeAreas: NavLink[] = navigationGroups[0].links.concat([{ href: "/literature", label: "الأدب" }]);

export const footerLinks: NavLink[] = [
  { href: "/search", label: "البحث" },
  ...navigationGroups.flatMap((group) => group.links),
];

export function uniqueLinks(links: NavLink[]): NavLink[] {
  const seen = new Set<string>();
  return links.filter((link) => {
    if (seen.has(link.href)) return false;
    seen.add(link.href);
    return true;
  });
}
