package com.mrsoft.arabicreference.contentimport.application;

import com.mrsoft.arabicreference.contentimport.domain.ImportIssue;
import com.mrsoft.arabicreference.contentimport.domain.ImportReport;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import com.mrsoft.arabicreference.contentimport.domain.ValidatedLexicalRecord;
import com.mrsoft.arabicreference.contentimport.infrastructure.ContentPackReader;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportBatchEntity;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportBatchRepository;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportRecordEntity;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportRecordRepository;
import com.mrsoft.arabicreference.dictionary.domain.ArabicLexicalText;
import com.mrsoft.arabicreference.dictionary.domain.FormType;
import com.mrsoft.arabicreference.dictionary.domain.PartOfSpeech;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.EntryCitationEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.EntryCitationRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalFormEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalFormRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalSenseEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalSenseRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.RootCitationEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.RootCitationRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.SenseCitationEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.SenseCitationRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ContentSlugs;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.source.infrastructure.persistence.ReferenceSourceEntity;
import com.mrsoft.arabicreference.source.infrastructure.persistence.SourceCitationEntity;
import com.mrsoft.arabicreference.source.infrastructure.persistence.SourceCitationRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ContentImportService {
    private static final UUID IMPORT_ACTOR = UUID.nameUUIDFromBytes("arabic-reference:content-import-service".getBytes(StandardCharsets.UTF_8));

    private final ContentPackReader reader;
    private final SourceResolver sourceResolver;
    private final ImportValidator validator;
    private final DuplicateDetector duplicateDetector;
    private final NormalizationService normalization;
    private final JsonMapper mapper;
    private final ContentImportBatchRepository batches;
    private final ContentImportRecordRepository importRecords;
    private final LexicalEntryRepository entries;
    private final LexicalSenseRepository senses;
    private final LexicalFormRepository forms;
    private final LinguisticRootRepository roots;
    private final SourceCitationRepository citations;
    private final EntryCitationRepository entryCitations;
    private final SenseCitationRepository senseCitations;
    private final RootCitationRepository rootCitations;
    private final TimeProvider time;

    public ContentImportService(
            ContentPackReader reader,
            SourceResolver sourceResolver,
            ImportValidator validator,
            DuplicateDetector duplicateDetector,
            NormalizationService normalization,
            JsonMapper mapper,
            ContentImportBatchRepository batches,
            ContentImportRecordRepository importRecords,
            LexicalEntryRepository entries,
            LexicalSenseRepository senses,
            LexicalFormRepository forms,
            LinguisticRootRepository roots,
            SourceCitationRepository citations,
            EntryCitationRepository entryCitations,
            SenseCitationRepository senseCitations,
            RootCitationRepository rootCitations,
            TimeProvider time) {
        this.reader = reader;
        this.sourceResolver = sourceResolver;
        this.validator = validator;
        this.duplicateDetector = duplicateDetector;
        this.normalization = normalization;
        this.mapper = mapper;
        this.batches = batches;
        this.importRecords = importRecords;
        this.entries = entries;
        this.senses = senses;
        this.forms = forms;
        this.roots = roots;
        this.citations = citations;
        this.entryCitations = entryCitations;
        this.senseCitations = senseCitations;
        this.rootCitations = rootCitations;
        this.time = time;
    }

    @Transactional
    public ImportReport importPack(Path file, String sourceSlug, boolean dryRun) {
        if (sourceSlug == null || sourceSlug.isBlank()) {
            throw new ContentImportException("A registered source slug is required.");
        }
        List<ImportedLexicalRecord> records = reader.read(file);
        if (records.isEmpty()) {
            throw new ContentImportException("Content pack contains no records.");
        }
        ReferenceSourceEntity source = sourceResolver.resolveForImport(sourceSlug);
        List<PlannedRecord> plan = new ArrayList<>();
        List<ImportIssue> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        Set<String> seenLemmas = new HashSet<>();
        int valid = 0;
        int duplicates = 0;
        int newRecords = 0;
        int updates = 0;

        for (int index = 0; index < records.size(); index++) {
            ImportedLexicalRecord input = records.get(index);
            int recordNumber = index + 1;
            ImportValidator.Result result = validator.validate(input, recordNumber);
            warnings.addAll(result.warnings());
            if (!result.valid()) {
                issues.addAll(result.issues());
                continue;
            }
            valid++;
            ValidatedLexicalRecord record = result.record();
            DuplicateDetector.Outcome outcome = duplicateDetector.inspect(source.getId(), record, seenKeys, seenLemmas);
            if (outcome.kind() == DuplicateDetector.Kind.DUPLICATE) {
                duplicates++;
                warnings.add("Record " + recordNumber + " (" + record.input().recordKey() + ") duplicates an input or existing dictionary entry and will be skipped.");
                continue;
            }
            String recordHash = recordHash(record.input());
            if (outcome.kind() == DuplicateDetector.Kind.PREVIOUSLY_IMPORTED) {
                if (outcome.imported().getContentSha256().equals(recordHash)) {
                    duplicates++;
                    continue;
                }
                LexicalEntryEntity existing = entries.findById(outcome.imported().getTargetId())
                        .orElseThrow(() -> new ContentImportException("Import tracking points to a missing lexical entry."));
                if (existing.getStatus() != PublicationStatus.DRAFT) {
                    issue(issues, recordNumber, record.input().recordKey(), "not-editable", "Changed records can only update an existing DRAFT entry.");
                    continue;
                }
                updates++;
                plan.add(new PlannedRecord(record, outcome, recordHash));
            } else {
                newRecords++;
                plan.add(new PlannedRecord(record, outcome, recordHash));
            }
        }

        int invalid = issues.stream().mapToInt(ignored -> 1).sum();
        if (invalid > 0) {
            warnings.add("No database changes were made because at least one record is invalid.");
        }
        boolean committed = !dryRun && invalid == 0;
        if (committed) {
            persistBatch(file, source, records.size(), valid, invalid, duplicates, newRecords, updates, plan);
        }
        return new ImportReport(
                source.getSlug(), dryRun, committed, records.size(), valid, invalid, duplicates,
                newRecords, updates, warnings, issues);
    }

    private void persistBatch(
            Path file,
            ReferenceSourceEntity source,
            int recordsRead,
            int valid,
            int invalid,
            int duplicates,
            int newRecords,
            int updates,
            List<PlannedRecord> plan) {
        Instant importedAt = time.now();
        UUID batchId = UUID.randomUUID();
        ContentImportBatchEntity batch = new ContentImportBatchEntity();
        batch.setId(batchId);
        batch.setSourceId(source.getId());
        batch.setFileName(file.getFileName().toString());
        batch.setFileSha256(fileHash(file));
        batch.setImportedAt(importedAt);
        batch.setRecordsRead(recordsRead);
        batch.setValidCount(valid);
        batch.setInvalidCount(invalid);
        batch.setDuplicateCount(duplicates);
        batch.setNewCount(newRecords);
        batch.setUpdateCount(updates);
        batch.setCreatedBy(IMPORT_ACTOR);
        batches.saveAndFlush(batch);

        for (PlannedRecord item : plan) {
            if (item.outcome().kind() == DuplicateDetector.Kind.NEW) {
                createRecord(source, item, batchId, importedAt);
            } else {
                updateRecord(source, item, batchId, importedAt);
            }
        }
    }

    private void createRecord(ReferenceSourceEntity source, PlannedRecord item, UUID batchId, Instant now) {
        ValidatedLexicalRecord record = item.record();
        UUID entryId = stableId("entry", source.getId() + ":" + record.input().recordKey().trim());
        UUID senseId = stableId("sense", source.getId() + ":" + record.input().recordKey().trim());
        UUID citationId = stableId("citation", source.getId() + ":" + record.input().recordKey().trim());
        LinguisticRootEntity root = resolveRoot(record, now);
        SourceCitationEntity citation = citation(source, record, citationId, now, batchId);
        citations.saveAndFlush(citation);

        LexicalEntryEntity entry = new LexicalEntryEntity();
        entry.setId(entryId);
        entry.setLemmaOriginal(record.input().lemma().trim());
        entry.setLemmaNormalized(ArabicLexicalText.requireLemma(record.normalizedLemma()));
        entry.setVocalizedForm(blankToNull(record.input().vocalizedForm()));
        entry.setRootId(root == null ? null : root.getId());
        entry.setPartOfSpeech(record.partOfSpeech());
        entry.setGender(record.gender());
        entry.setStatus(PublicationStatus.DRAFT);
        entry.setSlug(ContentSlugs.of(record.normalizedLemma(), entryId));
        entry.setChangeReason("Imported for editorial review; source attribution is linked.");
        stamp(entry, now);
        entries.saveAndFlush(entry);

        entryCitations.saveAndFlush(new EntryCitationEntity(entryId, citationId));
        if (hasDefinition(record)) {
            LexicalSenseEntity sense = new LexicalSenseEntity();
            sense.setId(senseId);
            sense.setLexicalEntryId(entryId);
            fillSense(sense, record, now, true);
            senses.saveAndFlush(sense);
            senseCitations.saveAndFlush(new SenseCitationEntity(senseId, citationId));
        }
        savePluralForm(entryId, record, now);
        linkRootCitation(root, citationId);
        saveImportRecord(source, record, entryId, citationId, item.contentHash(), batchId, now, null);
    }

    private void updateRecord(ReferenceSourceEntity source, PlannedRecord item, UUID batchId, Instant now) {
        ContentImportRecordEntity importRecord = item.outcome().imported();
        LexicalEntryEntity entry = entries.lockById(importRecord.getTargetId())
                .orElseThrow(() -> new ContentImportException("Import tracking points to a missing lexical entry."));
        ValidatedLexicalRecord record = item.record();
        LinguisticRootEntity root = resolveRoot(record, now);
        SourceCitationEntity citation = citations.findById(importRecord.getCitationId())
                .orElseThrow(() -> new ContentImportException("Import tracking points to a missing source citation."));
        citation.setSourceLocator(record.input().sourceLocator().trim());
        citation.setEntryLabel(record.input().lemma().trim());
        citation.setPageFrom(record.pageFrom());
        citation.setPageTo(record.pageTo());
        citations.saveAndFlush(citation);

        entry.setLemmaOriginal(record.input().lemma().trim());
        entry.setLemmaNormalized(ArabicLexicalText.requireLemma(record.normalizedLemma()));
        entry.setVocalizedForm(blankToNull(record.input().vocalizedForm()));
        entry.setRootId(root == null ? null : root.getId());
        entry.setPartOfSpeech(record.partOfSpeech());
        entry.setGender(record.gender());
        entry.setUpdatedAt(now);
        entry.setUpdatedBy(IMPORT_ACTOR);
        entries.saveAndFlush(entry);

        if (hasDefinition(record)) {
            List<LexicalSenseEntity> existingSenses = senses.findByLexicalEntryIdOrderByDisplayOrderAsc(entry.getId());
            LexicalSenseEntity sense;
            boolean isNewSense = existingSenses.isEmpty();
            if (isNewSense) {
                sense = new LexicalSenseEntity();
                sense.setId(stableId("sense", source.getId() + ":" + record.input().recordKey().trim()));
                sense.setLexicalEntryId(entry.getId());
            } else {
                sense = existingSenses.getFirst();
            }
            fillSense(sense, record, now, isNewSense);
            senses.saveAndFlush(sense);
            if (!senseCitations.existsByOwnerIdAndCitationId(sense.getId(), citation.getId())) {
                senseCitations.saveAndFlush(new SenseCitationEntity(sense.getId(), citation.getId()));
            }
        }
        savePluralForm(entry.getId(), record, now);
        if (!entryCitations.existsByOwnerIdAndCitationId(entry.getId(), citation.getId())) {
            entryCitations.saveAndFlush(new EntryCitationEntity(entry.getId(), citation.getId()));
        }
        linkRootCitation(root, citation.getId());
        saveImportRecord(source, record, entry.getId(), citation.getId(), item.contentHash(), batchId, now, importRecord);
    }

    private LinguisticRootEntity resolveRoot(ValidatedLexicalRecord record, Instant now) {
        if (record.normalizedRoot() == null) return null;
        return roots.findByRootNormalized(record.normalizedRoot()).orElseGet(() -> {
            LinguisticRootEntity root = new LinguisticRootEntity();
            UUID rootId = stableId("root", record.normalizedRoot());
            root.setId(rootId);
            root.setRootOriginal(record.input().root().trim());
            root.setRootNormalized(record.normalizedRoot());
            root.setRadicalCount((short) record.normalizedRoot().codePointCount(0, record.normalizedRoot().length()));
            root.setNotes(blankToNull(record.input().rootNote()));
            root.setStatus(PublicationStatus.DRAFT);
            root.setSlug(ContentSlugs.of(record.normalizedRoot(), rootId));
            root.setChangeReason("Imported root relation requires editorial review.");
            stamp(root, now);
            return roots.saveAndFlush(root);
        });
    }

    private SourceCitationEntity citation(
            ReferenceSourceEntity source, ValidatedLexicalRecord record, UUID id, Instant now, UUID batchId) {
        SourceCitationEntity citation = new SourceCitationEntity();
        citation.setId(id);
        citation.setSourceId(source.getId());
        citation.setEntryLabel(record.input().lemma().trim());
        citation.setSourceLocator(record.input().sourceLocator().trim());
        citation.setPageFrom(record.pageFrom());
        citation.setPageTo(record.pageTo());
        citation.setNotes("Import batch " + batchId + "; source record " + record.input().recordKey());
        citation.setCreatedAt(now);
        citation.setCreatedBy(IMPORT_ACTOR);
        return citation;
    }

    private void linkRootCitation(LinguisticRootEntity root, UUID citationId) {
        if (root != null && root.getStatus() == PublicationStatus.DRAFT
                && !rootCitations.existsByOwnerIdAndCitationId(root.getId(), citationId)) {
            rootCitations.saveAndFlush(new RootCitationEntity(root.getId(), citationId));
        }
    }

    private void savePluralForm(UUID entryId, ValidatedLexicalRecord record, Instant now) {
        String original = blankToNull(record.input().pluralForm());
        if (original == null) return;
        UUID formId = stableId("plural-form", entryId + ":" + original);
        LexicalFormEntity form = forms.findById(formId).orElseGet(LexicalFormEntity::new);
        boolean isNew = form.getId() == null;
        form.setId(formId);
        form.setLexicalEntryId(entryId);
        form.setFormType(FormType.PLURAL);
        form.setOriginalForm(original);
        form.setNormalizedForm(normalization.searchForm(original));
        form.setNotes("Imported as a source-provided plural form; requires editorial review.");
        form.setStatus(PublicationStatus.DRAFT);
        form.setDisplayOrder(Math.max(1, forms.maxOrder(entryId) + (isNew ? 1 : 0)));
        form.setUpdatedAt(now);
        form.setUpdatedBy(IMPORT_ACTOR);
        if (isNew) {
            form.setCreatedAt(now);
            form.setCreatedBy(IMPORT_ACTOR);
        }
        forms.saveAndFlush(form);
    }

    private void saveImportRecord(
            ReferenceSourceEntity source,
            ValidatedLexicalRecord record,
            UUID entryId,
            UUID citationId,
            String contentHash,
            UUID batchId,
            Instant now,
            ContentImportRecordEntity existing) {
        ContentImportRecordEntity importRecord = existing == null ? new ContentImportRecordEntity() : existing;
        importRecord.setId(existing == null
                ? stableId("import-record", source.getId() + ":" + record.input().recordKey())
                : existing.getId());
        importRecord.setSourceId(source.getId());
        importRecord.setSourceRecordKey(record.input().recordKey().trim());
        importRecord.setTargetType("DICTIONARY_ENTRY");
        importRecord.setTargetId(entryId);
        importRecord.setCitationId(citationId);
        importRecord.setContentSha256(contentHash);
        importRecord.setLastBatchId(batchId);
        importRecord.setImportedAt(now);
        importRecords.saveAndFlush(importRecord);
    }

    private static void fillSense(LexicalSenseEntity sense, ValidatedLexicalRecord record, Instant now, boolean isNew) {
        sense.setDefinition(record.input().definition().trim());
        sense.setShortDefinition(blankToNull(record.input().shortDefinition()));
        sense.setUsageLabel(record.usageLabel());
        sense.setDomainLabel(record.semanticDomain());
        sense.setDisplayOrder(1);
        sense.setStatus(PublicationStatus.DRAFT);
        sense.setUpdatedAt(now);
        sense.setUpdatedBy(IMPORT_ACTOR);
        if (isNew) {
            sense.setCreatedAt(now);
            sense.setCreatedBy(IMPORT_ACTOR);
        }
    }

    private static boolean hasDefinition(ValidatedLexicalRecord record) {
        return record.input().definition() != null && !record.input().definition().isBlank();
    }

    private static void stamp(LexicalEntryEntity entry, Instant now) {
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
        entry.setCreatedBy(IMPORT_ACTOR);
        entry.setUpdatedBy(IMPORT_ACTOR);
        entry.setVersion(0);
    }

    private static void stamp(LinguisticRootEntity root, Instant now) {
        root.setCreatedAt(now);
        root.setUpdatedAt(now);
        root.setCreatedBy(IMPORT_ACTOR);
        root.setUpdatedBy(IMPORT_ACTOR);
        root.setVersion(0);
    }

    private String recordHash(ImportedLexicalRecord record) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(record);
            return sha256(bytes);
        } catch (JacksonException exception) {
            throw new ContentImportException("Could not fingerprint imported record.", exception);
        }
    }

    private static String fileHash(Path file) {
        try {
            return sha256(Files.readAllBytes(file));
        } catch (IOException exception) {
            throw new ContentImportException("Could not fingerprint content pack.", exception);
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    private static UUID stableId(String purpose, String value) {
        return UUID.nameUUIDFromBytes((purpose + "\u0000" + value).getBytes(StandardCharsets.UTF_8));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void issue(List<ImportIssue> issues, int row, String key, String code, String message) {
        issues.add(new ImportIssue(row, key, code, message));
    }

    private record PlannedRecord(ValidatedLexicalRecord record, DuplicateDetector.Outcome outcome, String contentHash) {
    }
}
