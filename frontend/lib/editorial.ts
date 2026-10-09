export const emptyReviewMessage = "لا توجد مواد تنتظر المراجعة حاليًا.";

export type EditorialDashboard = {
  draft: number;
  inReview: number;
  changesRequested: number;
  verified: number;
  readyToPublish: number;
  published: number;
  archived: number;
  qualityIssues: number;
  emptyReview: boolean;
};

export type QueueItem = {
  contentType: string;
  id: string;
  title: string;
  slug: string;
  status: string;
  createdBy: string | null;
  reviewedBy: string | null;
  updatedAt: string | null;
  version: number;
  assigneeId: string | null;
  blockers: number;
  href: string;
};

export type QueuePage = {
  items: QueueItem[];
  total: number;
  page: number;
  size: number;
};

export type QualityFinding = {
  id: string;
  contentType: string;
  contentId: string;
  code: string;
  severity: string;
  message: string;
  field: string | null;
  detectedAt: string | null;
};

export function severityLabel(severity: string): string {
  if (severity === "BLOCKER") return "مانع للنشر";
  if (severity === "WARNING") return "تحذير";
  if (severity === "INFO") return "ملاحظة";
  return "غير مصنّف";
}

export function licenseLabel(license: string): string {
  switch (license) {
    case "PUBLIC_DOMAIN":
      return "ملكية عامة";
    case "CC0":
      return "ملكية عامة CC0";
    case "CC_BY":
      return "نسب المصنف";
    case "CC_BY_SA":
      return "نسب المصنف - المشاركة بالمثل";
    case "PERMISSION_GRANTED":
      return "إذن ممنوح";
    case "RESTRICTED":
      return "مقيد";
    case "UNKNOWN":
      return "غير معروف";
    default:
      return "غير معروف";
  }
}

export function statusLabel(status: string): string {
  switch (status) {
    case "DRAFT":
      return "مسودة";
    case "IN_REVIEW":
      return "قيد المراجعة";
    case "CHANGES_REQUESTED":
      return "طُلب تعديل";
    case "VERIFIED":
      return "تم التحقق";
    case "PUBLISHED":
      return "منشور";
    case "ARCHIVED":
      return "مؤرشف";
    default:
      return status;
  }
}

export function contentTypeLabel(type: string): string {
  switch (type) {
    case "DICTIONARY_ENTRY":
      return "مدخل معجم";
    case "MORPHOLOGY_ANALYSIS":
      return "تحليل صرفي";
    case "GRAMMAR_TOPIC":
      return "باب نحوي";
    case "GRAMMAR_RULE":
      return "قاعدة نحوية";
    case "GRAMMAR_CONCEPT":
      return "مفهوم نحوي";
    case "SPELLING_TOPIC":
      return "باب إملائي";
    case "SPELLING_RULE":
      return "قاعدة إملائية";
    case "RHETORIC_TOPIC":
      return "باب بلاغي";
    case "RHETORIC_DEVICE":
      return "أسلوب بلاغي";
    case "LITERARY_ERA":
      return "عصر أدبي";
    case "LITERARY_GENRE":
      return "جنس أدبي";
    case "LITERARY_SCHOOL":
      return "مدرسة أدبية";
    case "LITERARY_FIGURE":
      return "علم أدبي";
    case "LITERARY_WORK":
      return "عمل أدبي";
    case "ARTICLE":
      return "مقال";
    case "LEARNING_PATH":
      return "مسار تعلّم";
    case "SEARCH_INDEX":
      return "فهرس البحث";
    default:
      return type;
  }
}

export function recordHref(type: string, id: string): string {
  return `/admin/editorial/records/${type}/${id}`;
}

export function rightsFinding(code: string): boolean {
  return code === "RIGHTS_EXCERPT" || code === "MISSING_RIGHTS";
}

export function citationFinding(code: string): boolean {
  return code.includes("CITATION") || code === "BROKEN_SOURCE_REFERENCE";
}
