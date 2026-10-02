package com.mrsoft.arabicreference.dictionary.application;

import com.mrsoft.arabicreference.dictionary.domain.ExampleKind;
import com.mrsoft.arabicreference.dictionary.domain.FormType;
import com.mrsoft.arabicreference.dictionary.domain.GrammaticalGender;
import com.mrsoft.arabicreference.dictionary.domain.PartOfSpeech;
import com.mrsoft.arabicreference.dictionary.domain.RelationType;
import com.mrsoft.arabicreference.dictionary.domain.SemanticDomain;
import com.mrsoft.arabicreference.dictionary.domain.UsageLabel;
import com.mrsoft.arabicreference.dictionary.domain.VerificationLevel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DictionaryViews {

    private DictionaryViews() {
    }

    public record PageResult<T>(List<T> items, int page, int size, long total) {
    }

    public record Attribution(
            String title,
            String author,
            String edition,
            Integer publicationYear,
            Integer pageFrom,
            Integer pageTo,
            String volume,
            String locator,
            String attributionText) {
    }

    public record PublicExample(String textOriginal, String explanation, String kind) {
    }

    public record PublicSense(
            String definition,
            String shortDefinition,
            String usageLabel,
            String domainLabel,
            int displayOrder,
            List<PublicExample> examples,
            List<Attribution> sources) {
    }

    public record PublicForm(String formType, String originalForm) {
    }

    public record PublicRelation(String relationType, String otherLemma, String otherSlug, String direction) {
    }

    public record PublicRootRef(String original, String normalized, String slug) {
    }

    public record PublicEntry(
            UUID id,
            String slug,
            String lemmaOriginal,
            String vocalizedForm,
            String partOfSpeech,
            String gender,
            PublicRootRef root,
            List<PublicSense> senses,
            List<PublicForm> forms,
            List<PublicRelation> relations) {
    }

    public record LookupHit(
            UUID id,
            String slug,
            String lemmaOriginal,
            String vocalizedForm,
            String partOfSpeech,
            String root,
            String shortDefinition,
            int senseCount) {
    }

    public record PublicRoot(String original, String normalized, int radicalCount, String slug, List<LookupHit> entries) {
    }

    public record EntryDraft(String lemma, String vocalizedForm, UUID rootId, PartOfSpeech partOfSpeech, GrammaticalGender gender) {
    }

    public record SenseDraft(String definition, String shortDefinition, UsageLabel usageLabel, SemanticDomain domainLabel, Integer displayOrder) {
    }

    public record FormDraft(FormType formType, String originalForm, String notes) {
    }

    public record ExampleDraft(ExampleKind kind, String text, String explanation, UUID citationId, Integer displayOrder) {
    }

    public record RelationDraft(RelationType relationType, UUID targetEntryId, UUID sourceSenseId, UUID targetSenseId, VerificationLevel verificationLevel) {
    }

    public record AdminExample(UUID id, String kind, String textOriginal, String explanation, UUID citationId, int displayOrder, String status) {
    }

    public record AdminSense(
            UUID id,
            String definition,
            String shortDefinition,
            String usageLabel,
            String domainLabel,
            int displayOrder,
            String status,
            long version,
            List<UUID> citationIds,
            List<AdminExample> examples) {
    }

    public record AdminForm(UUID id, String formType, String originalForm, String normalizedForm, String notes, int displayOrder, String status) {
    }

    public record AdminRelation(
            UUID id,
            String relationType,
            UUID sourceEntryId,
            UUID targetEntryId,
            UUID sourceSenseId,
            UUID targetSenseId,
            String verificationLevel,
            String status) {
    }

    public record AdminEntry(
            UUID id,
            String lemmaOriginal,
            String lemmaNormalized,
            String vocalizedForm,
            UUID rootId,
            String partOfSpeech,
            String gender,
            String status,
            String slug,
            long version,
            boolean publiclyVisible,
            List<AdminSense> senses,
            List<AdminForm> forms,
            List<AdminRelation> relations) {
    }

    public record AdminRoot(
            UUID id,
            String rootOriginal,
            String rootNormalized,
            int radicalCount,
            String notes,
            String status,
            String slug,
            long version,
            boolean publiclyVisible) {
    }

    public record RevisionView(int revisionNumber, UUID actorId, String changeReason, Instant createdAt) {
    }
}
