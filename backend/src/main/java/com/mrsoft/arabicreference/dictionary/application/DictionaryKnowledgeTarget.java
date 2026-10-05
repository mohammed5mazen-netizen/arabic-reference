package com.mrsoft.arabicreference.dictionary.application;

import com.mrsoft.arabicreference.content.domain.KnowledgeTargetSource;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DictionaryKnowledgeTarget implements KnowledgeTargetSource {

    private final LexicalEntryRepository entries;
    private final LinguisticRootRepository roots;

    public DictionaryKnowledgeTarget(LexicalEntryRepository entries, LinguisticRootRepository roots) {
        this.entries = entries;
        this.roots = roots;
    }

    @Override
    public boolean supports(KnowledgeTargetType type) {
        return type == KnowledgeTargetType.DICTIONARY_ENTRY || type == KnowledgeTargetType.ROOT;
    }

    @Override
    public Optional<KnowledgeTarget> published(UUID id) {
        Optional<KnowledgeTarget> entry = entries.findById(id)
                .filter(item -> visible(item.getPublishedSnapshot(), item.getStatus()))
                .map(item -> target(KnowledgeTargetType.DICTIONARY_ENTRY, id, item.getPublishedSnapshot(), "lemmaOriginal", "/word/"));
        if (entry.isPresent()) {
            return entry;
        }
        return roots.findById(id)
                .filter(item -> visible(item.getPublishedSnapshot(), item.getStatus()))
                .map(item -> target(KnowledgeTargetType.ROOT, id, item.getPublishedSnapshot(), "original", "/root/"));
    }

    private static boolean visible(Map<String, Object> snapshot, PublicationStatus status) {
        return snapshot != null && status != PublicationStatus.ARCHIVED;
    }

    private static KnowledgeTarget target(KnowledgeTargetType type, UUID id, Map<String, Object> snapshot, String titleKey, String prefix) {
        String title = snapshot.get(titleKey) == null ? "" : String.valueOf(snapshot.get(titleKey));
        String slug = snapshot.get("slug") == null ? "" : String.valueOf(snapshot.get("slug"));
        return new KnowledgeTarget(type, id, title, prefix + slug);
    }
}
