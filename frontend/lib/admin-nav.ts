export type AdminNavItem = {
  href: string;
  label: string;
  permission?: string;
  anyOf?: string[];
};

export const adminNav: AdminNavItem[] = [
  { href: "/admin", label: "الرئيسية" },
  { href: "/admin/users", label: "المستخدمون", permission: "admin.user.view" },
  { href: "/admin/roles", label: "الأدوار والصلاحيات", permission: "admin.role.view" },
  { href: "/admin/audit", label: "سجل التدقيق", permission: "admin.audit.view" },
  { href: "/admin/dictionary", label: "المداخل", permission: "dictionary.entry.view" },
  { href: "/admin/dictionary/roots", label: "الجذور", permission: "dictionary.root.view" },
  { href: "/admin/sources", label: "المصادر", permission: "source.view" },
  { href: "/admin/review", label: "المراجعات", permission: "dictionary.entry.review" },
  { href: "/admin/morphology", label: "الصرف", permission: "morphology.view" },
  { href: "/admin/grammar", label: "النحو", permission: "grammar.topic.view" },
  { href: "/admin/search", label: "البحث", permission: "search.admin.view" },
  { href: "/admin/spelling", label: "الإملاء", anyOf: ["spelling.topic.view", "spelling.rule.review", "spelling.rule.publish"] },
  { href: "/admin/rhetoric", label: "البلاغة", anyOf: ["rhetoric.topic.view", "rhetoric.device.review", "rhetoric.device.publish"] },
  { href: "/admin/literature", label: "الأدب", anyOf: ["literature.view", "literature.review", "literature.publish"] },
  { href: "/admin/articles", label: "المقالات", anyOf: ["content.article.view", "content.article.review", "content.article.publish"] },
  { href: "/admin/tools", label: "الأدوات", permission: "tools.view" },
  { href: "/admin/ai", label: "المساعد", permission: "ai.admin.view" },
];

export function visibleAdminNav(permissions: readonly string[]): AdminNavItem[] {
  return adminNav.filter((item) => {
    if (item.permission == null && (item.anyOf == null || item.anyOf.length === 0)) return true;
    if (item.permission != null && permissions.includes(item.permission)) return true;
    return item.anyOf?.some((code) => permissions.includes(code)) ?? false;
  });
}

export function can(permissions: readonly string[], permission: string): boolean {
  return permissions.includes(permission);
}
