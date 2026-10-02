package com.mrsoft.arabicreference.dictionary.application;

import com.mrsoft.arabicreference.dictionary.domain.FormType;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalFormRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import java.util.List;
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
    private final JdbcTemplate jdbc;

    public PublishedDictionaryQuery(
            LexicalEntryRepository entries,
            LinguisticRootRepository roots,
            LexicalFormRepository forms,
            JdbcTemplate jdbc) {
        this.entries = entries;
        this.roots = roots;
        this.forms = forms;
        this.jdbc = jdbc;
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
