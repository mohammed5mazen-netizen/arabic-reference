package com.mrsoft.arabicreference.editorial;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.editorial.domain.ArabicDiff;
import com.mrsoft.arabicreference.editorial.domain.ContentType;
import com.mrsoft.arabicreference.editorial.domain.QualityProbe;
import com.mrsoft.arabicreference.editorial.domain.QualityRules;
import com.mrsoft.arabicreference.editorial.domain.QualitySeverity;
import com.mrsoft.arabicreference.editorial.domain.StructuredDiff;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EditorialRulesTest {

    @Test
    void universalAndDomainRulesAreDeterministic() {
        QualityProbe dictionary = probe(ContentType.DICTIONARY_ENTRY, "DRAFT", "كتاب", null, false, false)
                .withSense(0, 1, true);
        assertThat(codes(dictionary)).contains("ENTRY_WITHOUT_SENSE", "SENSE_WITHOUT_CITATION", "BROKEN_ROOT");

        QualityProbe published = probe(ContentType.GRAMMAR_RULE, "PUBLISHED", "الفاعل", "قصير", true, true)
                .withGrammar(0, 1, 0, true, false);
        assertThat(codes(published)).contains(
                "SHORT_SUMMARY", "MISSING_CITATION", "RULE_WITHOUT_COMPONENTS",
                "QUOTED_EXAMPLE_WITHOUT_CITATION", "DUPLICATE_SLUG", "MISSING_PUBLISHED_SNAPSHOT", "SEARCH_DOCUMENT_MISSING");

        QualityProbe spelling = probe(ContentType.SPELLING_RULE, "PUBLISHED", "الهمزة", "قاعدة الهمزة المتطرفة في الأسماء", true, true)
                .withSpelling(0, 1);
        assertThat(codes(spelling)).contains("PUBLISHED_RULE_WITHOUT_CITATION", "COMMON_MISTAKE_WITHOUT_EVIDENCE");

        QualityProbe literature = probe(ContentType.LITERARY_WORK, "DRAFT", "الديوان", "وصف وصفي كافٍ للعمل الأدبي", false, false)
                .withLiterature("RESTRICTED", 1);
        assertThat(codes(literature)).contains("RIGHTS_EXCERPT");

        QualityProbe article = probe(ContentType.ARTICLE, "VERIFIED", "مقال", "هذا ملخص كافٍ للمقال اللغوي المنشور", true, true)
                .withArticle(0, 0);
        assertThat(codes(article)).contains("ARTICLE_WITHOUT_SECTIONS", "MISSING_CITATION");

        QualityProbe learning = probe(ContentType.LEARNING_PATH, "DRAFT", "النحو", "مسار تعليمي واضح للمبتدئين", false, false)
                .withLearning(1, 1, 1);
        assertThat(codes(learning)).contains("LESSON_WITHOUT_OBJECTIVE", "INVALID_QUESTION", "UNPUBLISHED_KNOWLEDGE_REFERENCE", "LEARNING_REVIEW");
        assertThat(QualityRules.evaluate(learning).stream().filter(finding -> finding.code().equals("LEARNING_REVIEW")).findFirst().orElseThrow().severity())
                .isEqualTo(QualitySeverity.INFO);

        QualityProbe broken = probe(ContentType.ARTICLE, "NOPE", "", "ملخص طويل بما يكفي للفحص", false, false).withArticle(1, 1);
        assertThat(codes(broken)).contains("MISSING_TITLE", "INVALID_WORKFLOW_STATE");

        QualityProbe discoverability = probe(ContentType.DICTIONARY_ENTRY, "PUBLISHED", "كتاب", null, false, false).withDiscoverability(true, true);
        assertThat(codes(discoverability)).contains("INVALID_CANONICAL_SLUG", "DISCOVERABILITY_ORPHAN");
        assertThat(QualityRules.invalidStatus("NOPE")).isTrue();

        QualityProbe relation = probe(ContentType.DICTIONARY_ENTRY, "PUBLISHED", "علم", null, true, true).withRelations(1, 1, 1);
        assertThat(codes(relation)).contains("UNPUBLISHED_RELATION_TARGET", "ORPHAN_RELATION", "BROKEN_SOURCE_REFERENCE");
    }

    @Test
    void arabicDiffKeepsCombiningMarksTogether() {
        String before = "كِتاب";
        String after = "كِتابٌ";
        assertThat(ArabicDiff.preservesCombiningMark(before)).isTrue();
        String inserted = ArabicDiff.diff(before, after).stream().filter(segment -> segment.op() == ArabicDiff.Op.INSERT).map(ArabicDiff.Segment::text).findFirst().orElseThrow();
        assertThat(inserted).contains("\u064c");
        assertThat(ArabicDiff.preservesCombiningMark(inserted)).isTrue();
        assertThat(ArabicDiff.diff(before, after).stream().noneMatch(segment -> segment.text().equals("\u0650"))).isTrue();
    }

    @Test
    void diffHidesSecretsAndDescribesCitationChanges() {
        var changes = StructuredDiff.compare(
                Map.of("title", "الفاعل", "password", "secret", "citations", "مصدر أ"),
                Map.of("title", "الفاعل التام", "password", "other", "examples", "مثال"));
        assertThat(changes).extracting(StructuredDiff.Change::code).contains("TITLE_CHANGED", "CITATION_REMOVED", "EXAMPLE_EDITED");
        assertThat(changes).noneMatch(change -> change.field().contains("password"));
    }

    private static java.util.List<String> codes(QualityProbe probe) {
        return QualityRules.evaluate(probe).stream().map(QualityRules.FindingDraft::code).toList();
    }

    private static ProbeBuilder probe(ContentType type, String status, String title, String summary, boolean citationRequired, boolean summaryRequired) {
        return new ProbeBuilder(type, status, title, summary, citationRequired, summaryRequired);
    }

    private static final class ProbeBuilder {
        private final ContentType type;
        private final String status;
        private final String title;
        private final String summary;
        private final boolean citationRequired;
        private final boolean summaryRequired;
        private int senseCount = 1;
        private int sensesWithoutCitation;
        private boolean brokenRoot;
        private boolean unpublishedRoot;
        private int unpublishedRelations;
        private int orphanRelations;
        private int brokenCitations;
        private boolean duplicateSlug;
        private int componentCount = 1;
        private int quotedExamplesWithoutCitation;
        private int citationCount = 1;
        private String rights = "PUBLIC_DOMAIN";
        private int excerptCount;
        private int sectionCount = 1;
        private int lessonsWithoutObjective;
        private int invalidQuestions;
        private int unpublishedKnowledgeRefs;
        private int commonMistakesWithoutEvidence;
        private boolean invalidCanonical;
        private boolean unlinked;
        private boolean hasSnapshot = true;
        private boolean indexed = true;

        private ProbeBuilder(ContentType type, String status, String title, String summary, boolean citationRequired, boolean summaryRequired) {
            this.type = type;
            this.status = status;
            this.title = title;
            this.summary = summary;
            this.citationRequired = citationRequired;
            this.summaryRequired = summaryRequired;
        }

        private QualityProbe withSense(int senses, int bare, boolean broken) {
            this.senseCount = senses;
            this.sensesWithoutCitation = bare;
            this.brokenRoot = broken;
            this.citationCount = 0;
            return build();
        }

        private QualityProbe withGrammar(int components, int quotes, int citations, boolean duplicate, boolean snapshot) {
            this.componentCount = components;
            this.quotedExamplesWithoutCitation = quotes;
            this.citationCount = citations;
            this.duplicateSlug = duplicate;
            this.hasSnapshot = snapshot;
            this.indexed = false;
            return build();
        }

        private QualityProbe withSpelling(int citations, int mistakes) {
            this.citationCount = citations;
            this.commonMistakesWithoutEvidence = mistakes;
            return build();
        }

        private QualityProbe withLiterature(String rights, int excerpts) {
            this.rights = rights;
            this.excerptCount = excerpts;
            return build();
        }

        private QualityProbe withArticle(int sections, int citations) {
            this.sectionCount = sections;
            this.citationCount = citations;
            return build();
        }

        private QualityProbe withLearning(int lessons, int questions, int refs) {
            this.lessonsWithoutObjective = lessons;
            this.invalidQuestions = questions;
            this.unpublishedKnowledgeRefs = refs;
            return build();
        }

        private QualityProbe withDiscoverability(boolean invalid, boolean orphan) {
            this.invalidCanonical = invalid;
            this.unlinked = orphan;
            this.indexed = true;
            this.hasSnapshot = true;
            return build();
        }

        private QualityProbe withRelations(int unpublished, int orphans, int broken) {
            this.unpublishedRelations = unpublished;
            this.orphanRelations = orphans;
            this.brokenCitations = broken;
            this.unpublishedRoot = true;
            return build();
        }

        private QualityProbe build() {
            return new QualityProbe(type, UUID.randomUUID(), title, summary, status, summaryRequired, citationRequired, hasSnapshot, indexed,
                    senseCount, sensesWithoutCitation, brokenRoot, unpublishedRoot, unpublishedRelations, orphanRelations, brokenCitations,
                    duplicateSlug, componentCount, quotedExamplesWithoutCitation, citationCount, rights, excerptCount, sectionCount,
                    lessonsWithoutObjective, invalidQuestions, unpublishedKnowledgeRefs, commonMistakesWithoutEvidence, invalidCanonical, unlinked);
        }
    }
}
