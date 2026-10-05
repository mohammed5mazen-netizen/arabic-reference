import { textDirection } from "./site.ts";

export const emptySpellingMessage = "لا توجد موضوعات إملائية منشورة بعد.";
export const emptyRhetoricMessage = "لا توجد موضوعات بلاغية منشورة بعد.";
export const emptyLiteratureMessage = "لا توجد حقب منشورة بعد.";
export const emptyWorksMessage = "لا توجد أعمال منشورة لهذا الأديب.";
export const emptyArticlesMessage = "لا توجد مقالات منشورة بعد.";
export const excerptBlockedMessage = "لا يُعرض مقتطف عندما تكون حقوق العمل غير معروفة أو مقيّدة.";
export const poetryClass = "whitespace-pre-wrap leading-10";

export function knowledgeDirection(): string {
  return textDirection;
}

export function spellingTopicPath(slug: string): string {
  return `/spelling/${slug}`;
}

export function spellingRulePath(slug: string): string {
  return `/spelling/rules/${slug}`;
}

export function rhetoricDevicePath(slug: string): string {
  return `/rhetoric/devices/${slug}`;
}

export function literatureFigurePath(slug: string): string {
  return `/literature/figures/${slug}`;
}

export function literatureWorkPath(slug: string): string {
  return `/literature/works/${slug}`;
}

export function articlePath(slug: string): string {
  return `/articles/${slug}`;
}

export function rightsAllowExcerpt(rights: string | null | undefined): boolean {
  return rights === "PUBLIC_DOMAIN" || rights === "LICENSED";
}

export function rightsLabel(rights: string | null | undefined): string {
  switch (rights) {
    case "PUBLIC_DOMAIN":
      return "ملكية عامة";
    case "LICENSED":
      return "مرخّص";
    case "RESTRICTED":
      return "مقيّد";
    default:
      return "الحقوق غير محسومة";
  }
}

export function showsFullTextButton(): boolean {
  return false;
}

export function knowledgeCrumbs(section: string, href: string, tail: { label: string; href?: string }[]): { label: string; href?: string }[] {
  return [{ label: "الرئيسية", href: "/" }, { label: section, href }, ...tail];
}

export const spellingAdminSections = ["الموضوعات", "القواعد", "الأمثلة", "المراجعات"];
export const rhetoricAdminSections = ["الموضوعات", "الفنون", "الأمثلة", "الصلات", "المراجعات"];
export const literatureAdminSections = ["الحقبات", "الأعلام", "الأعمال", "الأنواع", "المدارس", "الحقوق", "المراجعات"];
export const articleAdminSections = ["المقالة", "الأقسام", "الاستشهادات", "المعرفة المرتبطة", "المراجعات"];

export function knowledgeWorkflowActions(status: string, permissions: readonly string[], edit: string, review: string, publish: string): string[] {
  if ((status === "DRAFT" || status === "CHANGES_REQUESTED") && permissions.includes(edit)) return ["submit"];
  if (status === "IN_REVIEW" && permissions.includes(review)) return ["verify", "request-changes"];
  if (status === "VERIFIED") {
    const actions: string[] = [];
    if (permissions.includes(review)) actions.push("request-changes");
    if (permissions.includes(publish)) actions.push("publish");
    return actions;
  }
  if (status === "PUBLISHED" && permissions.includes(publish)) return ["archive"];
  return [];
}

export function literaryRoleLabel(role: string): string {
  switch (role) {
    case "POET":
      return "شاعر";
    case "WRITER":
      return "كاتب";
    case "CRITIC":
      return "ناقد";
    case "LINGUIST":
      return "لغوي";
    case "SCHOLAR":
      return "عالم";
    case "PLAYWRIGHT":
      return "مسرحي";
    case "NOVELIST":
      return "روائي";
    default:
      return "دور آخر";
  }
}
