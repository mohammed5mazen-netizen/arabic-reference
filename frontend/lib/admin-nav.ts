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
];

export const futureAdminNav = ["المحتوى اللغوي", "المصادر", "المراجعات"];

export function visibleAdminNav(permissions: readonly string[]): AdminNavItem[] {
  return adminNav.filter((item) => item.permission == null || permissions.includes(item.permission));
}

export function can(permissions: readonly string[], permission: string): boolean {
  return permissions.includes(permission);
}
