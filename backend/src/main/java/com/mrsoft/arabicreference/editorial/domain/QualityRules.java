package com.mrsoft.arabicreference.editorial.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class QualityRules {

    public static final Set<String> STATUSES = Set.of(
            "DRAFT", "IN_REVIEW", "CHANGES_REQUESTED", "VERIFIED", "PUBLISHED", "ARCHIVED");

    private QualityRules() {
    }

    public record FindingDraft(String code, QualitySeverity severity, String message, String field) {
    }

    public static List<FindingDraft> evaluate(QualityProbe probe) {
        List<FindingDraft> findings = new ArrayList<>();
        if (probe.title() == null || probe.title().isBlank()) {
            findings.add(blocker("MISSING_TITLE", "العنوان المطلوب غير موجود.", "title"));
        }
        if (probe.summaryRequired() && (probe.summary() == null || probe.summary().strip().length() < 12)) {
            findings.add(finding(publishable(probe) ? QualitySeverity.BLOCKER : QualitySeverity.WARNING,
                    "SHORT_SUMMARY", "الملخص أقصر مما يلزم أو غير موجود.", "summary"));
        }
        if (probe.citationRequired() && probe.citationCount() == 0) {
            findings.add(blocker("MISSING_CITATION", "هذه المادة تحتاج استشهادًا قبل النشر.", "citation"));
        }
        if (probe.brokenCitations() > 0) {
            findings.add(blocker("BROKEN_SOURCE_REFERENCE", "هناك إحالة إلى مصدر لم يعد موجودًا.", "citation"));
        }
        if (probe.duplicateSlug()) {
            findings.add(blocker("DUPLICATE_SLUG", "المسار المستعار مكرر.", "slug"));
        }
        if (probe.orphanRelations() > 0) {
            findings.add(blocker("ORPHAN_RELATION", "هناك علاقة بلا طرف مقابل.", "relation"));
        }
        if (probe.unpublishedRelations() > 0 || probe.unpublishedRoot()) {
            findings.add(blocker("UNPUBLISHED_RELATION_TARGET", "العلاقة تشير إلى مادة غير منشورة.", "relation"));
        }
        if (!STATUSES.contains(probe.status())) {
            findings.add(blocker("INVALID_WORKFLOW_STATE", "حالة سير العمل غير معروفة.", "status"));
        }
        if ("PUBLISHED".equals(probe.status()) && !probe.hasSnapshot()) {
            findings.add(blocker("MISSING_PUBLISHED_SNAPSHOT", "المادة المنشورة بلا لقطة نشر.", "snapshot"));
        }
        if (probe.type().searchable() && "PUBLISHED".equals(probe.status()) && !probe.indexed()) {
            findings.add(blocker("SEARCH_DOCUMENT_MISSING", "وثيقة البحث غير موجودة للمادة المنشورة.", "search"));
        }
        if ("PUBLISHED".equals(probe.status()) && probe.invalidCanonical()) {
            findings.add(finding(QualitySeverity.WARNING, "INVALID_CANONICAL_SLUG", "المسار العام غير صالح كعنوان متعارف.", "slug"));
        }
        if ("PUBLISHED".equals(probe.status()) && probe.unlinked()) {
            findings.add(finding(QualitySeverity.INFO, "DISCOVERABILITY_ORPHAN", "مدخل منشور بلا جذر وبلا علاقة داخلية.", "relation"));
        }
        switch (probe.type()) {
            case DICTIONARY_ENTRY -> dictionary(probe, findings);
            case GRAMMAR_RULE -> grammar(probe, findings);
            case SPELLING_RULE -> spelling(probe, findings);
            case LITERARY_WORK -> literature(probe, findings);
            case ARTICLE -> article(probe, findings);
            case LEARNING_PATH -> learning(probe, findings);
            default -> {
            }
        }
        return List.copyOf(findings);
    }

    public static boolean invalidStatus(String status) {
        return status == null || !STATUSES.contains(status);
    }

    private static void dictionary(QualityProbe probe, List<FindingDraft> findings) {
        if (probe.senseCount() == 0) {
            findings.add(blocker("ENTRY_WITHOUT_SENSE", "المدخل بلا معنى.", "sense"));
        }
        if (probe.sensesWithoutCitation() > 0) {
            findings.add(blocker("SENSE_WITHOUT_CITATION", "معنى يحتاج استشهادًا بلا مصدر.", "citation"));
        }
        if (probe.brokenRoot()) {
            findings.add(blocker("BROKEN_ROOT", "الجذر المرتبط غير موجود.", "root"));
        }
    }

    private static void grammar(QualityProbe probe, List<FindingDraft> findings) {
        if (publishable(probe) && probe.componentCount() == 0) {
            findings.add(blocker("RULE_WITHOUT_COMPONENTS", "القاعدة بلا مكوّنات مطلوبة.", "component"));
        }
        if (probe.quotedExamplesWithoutCitation() > 0) {
            findings.add(blocker("QUOTED_EXAMPLE_WITHOUT_CITATION", "مثال منقول بلا استشهاد.", "example"));
        }
    }

    private static void spelling(QualityProbe probe, List<FindingDraft> findings) {
        if ("PUBLISHED".equals(probe.status()) && probe.citationCount() == 0) {
            findings.add(blocker("PUBLISHED_RULE_WITHOUT_CITATION", "قاعدة إملائية منشورة بلا استشهاد.", "citation"));
        }
        if (probe.commonMistakesWithoutEvidence() > 0) {
            findings.add(finding(QualitySeverity.WARNING, "COMMON_MISTAKE_WITHOUT_EVIDENCE", "خطأ شائع بلا شاهد.", "example"));
        }
    }

    private static void literature(QualityProbe probe, List<FindingDraft> findings) {
        if (probe.rights() == null || probe.rights().isBlank()) {
            findings.add(blocker("MISSING_RIGHTS", "بيانات الحقوق غير موجودة.", "rights"));
        }
        if (("UNKNOWN".equals(probe.rights()) || "RESTRICTED".equals(probe.rights())) && probe.excerptCount() > 0) {
            findings.add(blocker("RIGHTS_EXCERPT", "مقتطف مع حقوق مقيدة أو غير معروفة.", "excerpt"));
        }
    }

    private static void article(QualityProbe probe, List<FindingDraft> findings) {
        if (publishable(probe) && probe.sectionCount() == 0) {
            findings.add(blocker("ARTICLE_WITHOUT_SECTIONS", "المقال بلا أقسام.", "section"));
        }
    }

    private static void learning(QualityProbe probe, List<FindingDraft> findings) {
        if (probe.lessonsWithoutObjective() > 0) {
            findings.add(blocker("LESSON_WITHOUT_OBJECTIVE", "درس بلا هدف تعلّمي.", "objective"));
        }
        if (probe.invalidQuestions() > 0) {
            findings.add(blocker("INVALID_QUESTION", "بنية سؤال الاختبار غير صالحة.", "question"));
        }
        if (probe.unpublishedKnowledgeRefs() > 0) {
            findings.add(blocker("UNPUBLISHED_KNOWLEDGE_REFERENCE", "مرجع المعرفة لم يعد منشورًا.", "reference"));
        }
        findings.add(finding(QualitySeverity.INFO, "LEARNING_REVIEW", "راجع أهداف الدرس قبل النشر.", "objective"));
    }

    private static boolean publishable(QualityProbe probe) {
        return "VERIFIED".equals(probe.status()) || "PUBLISHED".equals(probe.status());
    }

    private static FindingDraft blocker(String code, String message, String field) {
        return finding(QualitySeverity.BLOCKER, code, message, field);
    }

    private static FindingDraft finding(QualitySeverity severity, String code, String message, String field) {
        return new FindingDraft(code, severity, message, field);
    }
}
