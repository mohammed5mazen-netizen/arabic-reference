export type AdminNavItem = {
  href: string;
  label: string;
  permission?: string;
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
];

export function visibleAdminNav(permissions: readonly string[]): AdminNavItem[] {
  return adminNav.filter((item) => item.permission == null || permissions.includes(item.permission));
}

export function can(permissions: readonly string[], permission: string): boolean {
  return permissions.includes(permission);
}
