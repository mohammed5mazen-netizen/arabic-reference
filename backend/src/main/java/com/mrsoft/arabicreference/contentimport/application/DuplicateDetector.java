package com.mrsoft.arabicreference.contentimport.application;

import com.mrsoft.arabicreference.contentimport.domain.ValidatedLexicalRecord;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryRepository;
import com.mrsoft.arabicreference.dictionary.domain.PartOfSpeech;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportRecordEntity;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportRecordRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DuplicateDetector {
    private final LexicalEntryRepository entries;
    private final ContentImportRecordRepository importRecords;

    public DuplicateDetector(LexicalEntryRepository entries, ContentImportRecordRepository importRecords) {
        this.entries = entries;
        this.importRecords = importRecords;
    }

    public Outcome inspect(
            UUID sourceId,
            ValidatedLexicalRecord record,
            Set<String> seenKeys,
            Set<String> seenLemmas) {
        String key = record.input().recordKey();
        String normalizedKey = record.normalizedLemma() + "\u0000" + record.partOfSpeech().name();
        if (!seenKeys.add(key) || !seenLemmas.add(normalizedKey)) {
            return new Outcome(Kind.DUPLICATE, null, null);
        }
        ContentImportRecordEntity imported = importRecords.findBySourceIdAndSourceRecordKey(sourceId, key).orElse(null);
        if (imported != null) {
            return new Outcome(Kind.PREVIOUSLY_IMPORTED, imported, null);
        }
        var existing = entries.findByLemmaNormalizedAndPartOfSpeech(record.normalizedLemma(), record.partOfSpeech());
        if (existing.isPresent()) {
            return new Outcome(Kind.DUPLICATE, null, existing.get().getId());
        }
        return new Outcome(Kind.NEW, null, null);
    }

    public enum Kind {
        NEW,
        PREVIOUSLY_IMPORTED,
        DUPLICATE
    }

    public record Outcome(Kind kind, ContentImportRecordEntity imported, UUID existingEntryId) {
    }
}
