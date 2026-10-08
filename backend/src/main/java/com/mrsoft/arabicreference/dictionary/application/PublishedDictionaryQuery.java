package com.mrsoft.arabicreference.dictionary.application;

import com.mrsoft.arabicreference.dictionary.domain.FormType;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalFormRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalSenseRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRelationEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRelationRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Published dictionary reads for other modules. Callers do not use dictionary repositories.
 */
@Service
public class PublishedDictionaryQuery {

    private final LexicalEntryRepository entries;
    private final LinguisticRootRepository roots;
    private final LexicalFormRepository forms;
    private final LexicalSenseRepository senses;
    private final LinguisticRelationRepository relations;
    private final JdbcTemplate jdbc;

    public PublishedDictionaryQuery(
            LexicalEntryRepository entries,
            LinguisticRootRepository roots,
            LexicalFormRepository forms,
            LexicalSenseRepository senses,
            LinguisticRelationRepository relations,
            JdbcTemplate jdbc) {
        this.entries = entries;
        this.roots = roots;
        this.forms = forms;
        this.senses = senses;
        this.relations = relations;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Optional<PublishedLemma> lemmaBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return entries.findBySlug(slug).filter(this::published).map(this::lemma);
    }

    @Transactional(readOnly = true)
    public List<PublishedLemma> lemmas(String normalizedLemma) {
        return entries.findPublishedByLemma(normalizedLemma, PublicationStatus.ARCHIVED, PageRequest.of(0, 20))
                .map(this::lemma)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PublishedLemma> lemma(UUID id) {
        return entries.findById(id).filter(this::published).map(this::lemma);
    }

    @Transactional(readOnly = true)
    public Optional<PublishedRoot> root(String normalizedRoot) {
        return roots.findByRootNormalized(normalizedRoot).filter(this::publishedRoot).map(this::rootView);
    }

    @Transactional(readOnly = true)
    public Optional<PublishedRoot> rootBySlug(String slug) {
        return roots.findPublished(slug, PublicationStatus.ARCHIVED).map(this::rootView);
    }

    @Transactional(readOnly = true)
    public List<PublishedLemma> entriesForRoot(UUID rootId) {
        return entries.findPublishedByRoot(rootId, PublicationStatus.ARCHIVED).stream().map(this::lemma).toList();
    }

    @Transactional(readOnly = true)
    public List<PublishedLemma> entriesForForm(String normalizedForm) {
        List<PublishedLemma> matches = new ArrayList<>();
        for (var form : forms.findPublishedByNormalized(normalizedForm)) {
            if (matches.size() >= 20) {
                break;
            }
            lemma(form.getLexicalEntryId()).ifPresent(matches::add);
        }
        return matches;
    }

    @Transactional(readOnly = true)
    public List<PublishedSense> publishedSenses(UUID entryId) {
        return senses.findByLexicalEntryIdOrderByDisplayOrderAsc(entryId).stream()
                .filter(sense -> sense.getStatus() == PublicationStatus.PUBLISHED)
                .map(sense -> new PublishedSense(
                        sense.getId(),
                        sense.getDisplayOrder(),
                        sense.getDefinition(),
                        sense.getShortDefinition(),
                        sense.getUsageLabel() == null ? null : sense.getUsageLabel().name(),
                        sense.getDomainLabel() == null ? null : sense.getDomainLabel().name()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublishedRelation> publishedRelations(UUID entryId) {
        Map<UUID, PublishedLemma> others = new LinkedHashMap<>();
        List<PublishedRelation> result = new ArrayList<>();
        for (LinguisticRelationEntity relation : relations.findTouching(entryId)) {
            if (relation.getStatus() != PublicationStatus.PUBLISHED || result.size() >= 40) {
                continue;
            }
            boolean outgoing = relation.getSourceEntryId().equals(entryId);
            UUID otherId = outgoing ? relation.getTargetEntryId() : relation.getSourceEntryId();
            PublishedLemma other = others.computeIfAbsent(otherId, id -> lemma(id).orElse(null));
            if (other == null) {
                continue;
            }
            UUID localSense = outgoing ? relation.getSourceSenseId() : relation.getTargetSenseId();
            result.add(new PublishedRelation(
                    localSense,
                    relation.getRelationType().name(),
                    other.lemmaOriginal(),
                    other.slug(),
                    outgoing ? "outgoing" : "incoming"));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<String> recordedPlurals(UUID entryId) {
        return forms.findByLexicalEntryIdOrderByDisplayOrderAsc(entryId).stream()
                .filter(form -> form.getFormType() == FormType.PLURAL && form.getStatus() == PublicationStatus.PUBLISHED)
                .map(form -> form.getOriginalForm())
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<EditableLemma> editableEntry(UUID id) {
        return entries.findById(id).map(entry -> {
            LinguisticRootEntity root = entry.getRootId() == null ? null : roots.findById(entry.getRootId()).orElse(null);
            return new EditableLemma(
                    entry.getId(),
                    entry.getLemmaOriginal(),
                    entry.getLemmaNormalized(),
                    entry.getRootId(),
                    root == null ? null : root.getRootOriginal(),
                    root == null ? null : (int) root.getRadicalCount(),
                    entry.getPartOfSpeech().name(),
                    entry.getStatus());
        });
    }

    @Transactional(readOnly = true)
    public String contentStamp() {
        String stamp = jdbc.queryForObject("""
                select greatest(
                    coalesce((select max(updated_at) from lexical_entry), timestamptz '1970-01-01'),
                    coalesce((select max(updated_at) from linguistic_root), timestamptz '1970-01-01')
                )::text
                """, String.class);
        return stamp == null ? "0" : stamp;
    }

    private boolean published(LexicalEntryEntity entry) {
        return entry.getPublishedSnapshot() != null && entry.getStatus() != PublicationStatus.ARCHIVED;
    }

    private boolean publishedRoot(LinguisticRootEntity root) {
        return root.getPublishedSnapshot() != null && root.getStatus() != PublicationStatus.ARCHIVED;
    }

    private PublishedLemma lemma(LexicalEntryEntity entry) {
        LinguisticRootEntity root = entry.getRootId() == null ? null : roots.findById(entry.getRootId()).orElse(null);
        boolean rootVisible = root != null && publishedRoot(root);
        return new PublishedLemma(
                entry.getId(),
                entry.getLemmaOriginal(),
                entry.getLemmaNormalized(),
                entry.getVocalizedForm(),
                rootVisible ? root.getId() : null,
                rootVisible ? root.getRootOriginal() : null,
                rootVisible ? root.getRootNormalized() : null,
                rootVisible ? (int) root.getRadicalCount() : null,
                entry.getPartOfSpeech().name(),
                entry.getSlug());
    }

    private PublishedRoot rootView(LinguisticRootEntity root) {
        return new PublishedRoot(root.getId(), root.getRootOriginal(), root.getRootNormalized(), root.getRadicalCount(), root.getSlug());
    }

    public record PublishedLemma(
            UUID id,
            String lemmaOriginal,
            String lemmaNormalized,
            String vocalizedForm,
            UUID rootId,
            String rootOriginal,
            String rootNormalized,
            Integer radicalCount,
            String partOfSpeech,
            String slug) {
    }

    public record PublishedRoot(UUID id, String original, String normalized, int radicalCount, String slug) {
    }

    public record PublishedSense(UUID id, int displayOrder, String definition, String shortDefinition, String usageLabel, String domainLabel) {
    }

    public record PublishedRelation(UUID senseId, String relationType, String otherLemma, String otherSlug, String direction) {
    }

    public record EditableLemma(
            UUID id,
            String lemmaOriginal,
            String lemmaNormalized,
            UUID rootId,
            String rootOriginal,
            Integer radicalCount,
            String partOfSpeech,
            PublicationStatus status) {
    }
}
