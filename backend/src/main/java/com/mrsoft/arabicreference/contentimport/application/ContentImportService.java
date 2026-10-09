package com.mrsoft.arabicreference.contentimport.application;

import com.mrsoft.arabicreference.contentimport.domain.ImportIssue;
import com.mrsoft.arabicreference.contentimport.domain.ImportReport;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import com.mrsoft.arabicreference.contentimport.domain.ValidatedLexicalRecord;
import com.mrsoft.arabicreference.contentimport.domain.ValidatedLexicalRecord.ValidatedForm;
import com.mrsoft.arabicreference.contentimport.domain.ValidatedLexicalRecord.ValidatedSense;
import com.mrsoft.arabicreference.contentimport.infrastructure.ContentPackReader;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportBatchEntity;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportBatchRepository;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportRecordEntity;
import com.mrsoft.arabicreference.contentimport.infrastructure.persistence.ContentImportRecordRepository;
import com.mrsoft.arabicreference.dictionary.domain.ArabicLexicalText;
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
import java.io.InputStream;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ContentImportService {
    private final ContentPackReader reader;
    private final ImportActorResolver actorResolver;
    private final SourceResolver sourceResolver;
    private final ImportValidator validator;
    private final DuplicateDetector duplicateDetector;
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
    private final TransactionTemplate batchTransactions;
    private final int batchSize;

    public ContentImportService(
            ContentPackReader reader,
            ImportActorResolver actorResolver,
            SourceResolver sourceResolver,
            ImportValidator validator,
            DuplicateDetector duplicateDetector,
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
            TimeProvider time,
            PlatformTransactionManager transactionManager,
            @Value("${content.import.batch-size:500}") int batchSize) {
        if (batchSize < 1 || batchSize > 5000) {
            throw new IllegalArgumentException("content.import.batch-size must be between 1 and 5000.");
        }
        this.reader = reader;
        this.actorResolver = actorResolver;
        this.sourceResolver = sourceResolver;
        this.validator = validator;
        this.duplicateDetector = duplicateDetector;
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
        this.batchTransactions = new TransactionTemplate(transactionManager);
        this.batchSize = batchSize;
    }

    public ImportReport importPack(Path file, String sourceSlug, boolean dryRun) {
        if (sourceSlug == null || sourceSlug.isBlank()) {
            throw new ContentImportException("A registered source slug is required.");
        }
        ReferenceSourceEntity source = sourceResolver.resolveForImport(sourceSlug);
        UUID actorId = actorResolver.requireDisabledActor();
        String fileSha256 = fileHash(file);
        Analysis analysis = analyze(file, source);
        if (analysis.recordsRead() == 0) {
            throw new ContentImportException("Content pack contains no records.");
        }
        if (analysis.invalid() > 0) {
            analysis.addWarning("No database changes were made because at least one record is invalid.");
        }
        boolean committed = !dryRun && analysis.invalid() == 0;
        if (committed) {
            if (!fileSha256.equals(fileHash(file))) {
                throw new ContentImportException("Content pack changed during validation; no records were imported.");
            }
            importValidatedPack(file, fileSha256, source, actorId);
        }
        return new ImportReport(
                source.getSlug(), dryRun, committed, analysis.recordsRead(), analysis.valid(), analysis.invalid(),
                analysis.duplicates(), analysis.newRecords(), analysis.updates(), analysis.warnings(), analysis.issues());
    }

    private Analysis analyze(Path file, ReferenceSourceEntity source) {
        Analysis analysis = new Analysis();
        Set<String> seenKeys = new HashSet<>();
        Set<String> seenLemmas = new HashSet<>();
        int[] rowNumber = {0};
        analysis.setRecordsRead(reader.forEachRecord(file, input -> {
            int recordNumber = ++rowNumber[0];
            ImportValidator.Result result = validator.validate(input, recordNumber);
            analysis.addWarnings(result.warnings());
            if (!result.valid()) {
                analysis.addInvalid(result.issues());
                return;
            }
            analysis.incrementValid();
            ValidatedLexicalRecord record = result.record();
            DuplicateDetector.Outcome outcome = duplicateDetector.inspect(source.getId(), record, seenKeys, seenLemmas);
            if (outcome.kind() == DuplicateDetector.Kind.DUPLICATE) {
                analysis.incrementDuplicates();
                analysis.addWarning("Record " + recordNumber + " duplicates an input or existing dictionary entry and will be skipped.");
                return;
            }
            String recordHash = recordHash(record.input());
            if (outcome.kind() == DuplicateDetector.Kind.PREVIOUSLY_IMPORTED) {
                if (outcome.imported().getContentSha256().equals(recordHash)) {
                    analysis.incrementDuplicates();
                    return;
                }
                LexicalEntryEntity existing = entries.findById(outcome.imported().getTargetId())
                        .orElseThrow(() -> new ContentImportException("Import tracking points to a missing lexical entry."));
                if (existing.getStatus() != PublicationStatus.DRAFT) {
                    analysis.addInvalid(new ImportIssue(
                            recordNumber, record.input().recordKey(), "not-editable",
                            "Changed records can only update an existing DRAFT entry."));
                    return;
                }
                analysis.incrementUpdates();
            } else {
                analysis.incrementNewRecords();
            }
        }));
        return analysis;
    }

    private void importValidatedPack(Path file, String fileSha256, ReferenceSourceEntity source, UUID actorId) {
        List<PlannedRecord> batch = new ArrayList<>(batchSize);
        Set<String> seenKeys = new HashSet<>();
        Set<String> seenLemmas = new HashSet<>();
        int[] rowNumber = {0};
        reader.forEachRecord(file, input -> {
            ImportValidator.Result result = validator.validate(input, ++rowNumber[0]);
            if (!result.valid()) {
                throw new ContentImportException("Content pack no longer matches its successful validation report.");
            }
            ValidatedLexicalRecord record = result.record();
            DuplicateDetector.Outcome outcome = duplicateDetector.inspect(source.getId(), record, seenKeys, seenLemmas);
            if (outcome.kind() == DuplicateDetector.Kind.DUPLICATE) return;
            String contentHash = recordHash(record.input());
            if (outcome.kind() == DuplicateDetector.Kind.PREVIOUSLY_IMPORTED) {
                if (outcome.imported().getContentSha256().equals(contentHash)) return;
                LexicalEntryEntity existing = entries.findById(outcome.imported().getTargetId())
                        .orElseThrow(() -> new ContentImportException("Import tracking points to a missing lexical entry."));
                if (existing.getStatus() != PublicationStatus.DRAFT) {
                    throw new ContentImportException("A previously imported entry changed after validation; retry dry-run.");
                }
            }
            batch.add(new PlannedRecord(record, outcome, contentHash));
            if (batch.size() == batchSize) {
                persistChunk(file, fileSha256, source, actorId, batch);
                batch.clear();
            }
        });
        if (!batch.isEmpty()) persistChunk(file, fileSha256, source, actorId, batch);
    }

    private void persistChunk(Path file, String fileSha256, ReferenceSourceEntity source, UUID actorId, List<PlannedRecord> items) {
        List<PlannedRecord> stableChunk = List.copyOf(items);
        batchTransactions.executeWithoutResult(status ->
                persistBatch(file.getFileName().toString(), fileSha256, source, actorId, stableChunk));
    }

    private void persistBatch(
            String fileName,
            String fileSha256,
            ReferenceSourceEntity source,
            UUID actorId,
            List<PlannedRecord> plan) {
        Instant importedAt = time.now();
        UUID batchId = UUID.randomUUID();
        ContentImportBatchEntity batch = new ContentImportBatchEntity();
        batch.setId(batchId);
        batch.setSourceId(source.getId());
        batch.setFileName(fileName);
        batch.setFileSha256(fileSha256);
        batch.setImportedAt(importedAt);
        batch.setRecordsRead(plan.size());
        batch.setValidCount(plan.size());
        batch.setInvalidCount(0);
        batch.setDuplicateCount(0);
        batch.setNewCount((int) plan.stream().filter(item -> item.outcome().kind() == DuplicateDetector.Kind.NEW).count());
        batch.setUpdateCount(plan.size() - batch.getNewCount());
        batch.setCreatedBy(actorId);
        batches.saveAndFlush(batch);

        for (PlannedRecord item : plan) {
            if (item.outcome().kind() == DuplicateDetector.Kind.NEW) {
                createRecord(source, item, batchId, importedAt, actorId);
            } else {
                updateRecord(source, item, batchId, importedAt, actorId);
            }
        }
    }

    private void createRecord(ReferenceSourceEntity source, PlannedRecord item, UUID batchId, Instant now, UUID actorId) {
        ValidatedLexicalRecord record = item.record();
        UUID entryId = stableId("entry", source.getId() + ":" + record.input().recordKey().trim());
        UUID citationId = stableId("citation", source.getId() + ":" + record.input().recordKey().trim());
        LinguisticRootEntity root = resolveRoot(record, now, actorId);
        SourceCitationEntity citation = citation(source, record, citationId, now, batchId, actorId);
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
        stamp(entry, now, actorId);
        entries.saveAndFlush(entry);

        entryCitations.saveAndFlush(new EntryCitationEntity(entryId, citationId));
        saveSenses(source, record, entryId, citationId, now, actorId);
        saveForms(entryId, record.forms(), now, actorId);
        linkRootCitation(root, citationId);
        saveImportRecord(source, record, entryId, citationId, item.contentHash(), batchId, now, null);
    }

    private void updateRecord(ReferenceSourceEntity source, PlannedRecord item, UUID batchId, Instant now, UUID actorId) {
        ContentImportRecordEntity importRecord = item.outcome().imported();
        LexicalEntryEntity entry = entries.lockById(importRecord.getTargetId())
                .orElseThrow(() -> new ContentImportException("Import tracking points to a missing lexical entry."));
        ValidatedLexicalRecord record = item.record();
        LinguisticRootEntity root = resolveRoot(record, now, actorId);
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
        entry.setUpdatedBy(actorId);
        entries.saveAndFlush(entry);

        saveSenses(source, record, entry.getId(), citation.getId(), now, actorId);
        saveForms(entry.getId(), record.forms(), now, actorId);
        if (!entryCitations.existsByOwnerIdAndCitationId(entry.getId(), citation.getId())) {
            entryCitations.saveAndFlush(new EntryCitationEntity(entry.getId(), citation.getId()));
        }
        linkRootCitation(root, citation.getId());
        saveImportRecord(source, record, entry.getId(), citation.getId(), item.contentHash(), batchId, now, importRecord);
    }

    private LinguisticRootEntity resolveRoot(ValidatedLexicalRecord record, Instant now, UUID actorId) {
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
            stamp(root, now, actorId);
            return roots.saveAndFlush(root);
        });
    }

    private SourceCitationEntity citation(
            ReferenceSourceEntity source, ValidatedLexicalRecord record, UUID id, Instant now, UUID batchId, UUID actorId) {
        SourceCitationEntity citation = new SourceCitationEntity();
        citation.setId(id);
        citation.setSourceId(source.getId());
        citation.setEntryLabel(record.input().lemma().trim());
        citation.setSourceLocator(record.input().sourceLocator().trim());
        citation.setPageFrom(record.pageFrom());
        citation.setPageTo(record.pageTo());
        citation.setNotes("Import batch " + batchId + "; source record " + record.input().recordKey());
        citation.setCreatedAt(now);
        citation.setCreatedBy(actorId);
        return citation;
    }

    private void linkRootCitation(LinguisticRootEntity root, UUID citationId) {
        if (root != null && root.getStatus() == PublicationStatus.DRAFT
                && !rootCitations.existsByOwnerIdAndCitationId(root.getId(), citationId)) {
            rootCitations.saveAndFlush(new RootCitationEntity(root.getId(), citationId));
        }
    }

    private void saveSenses(
            ReferenceSourceEntity source,
            ValidatedLexicalRecord record,
            UUID entryId,
            UUID citationId,
            Instant now,
            UUID actorId) {
        for (ValidatedSense value : record.senses()) {
            String key = source.getId() + ":" + record.input().recordKey().trim()
                    + (value.displayOrder() == 1 ? "" : ":" + value.displayOrder());
            UUID senseId = stableId("sense", key);
            LexicalSenseEntity sense = senses.findById(senseId).orElseGet(LexicalSenseEntity::new);
            boolean isNew = sense.getId() == null;
            sense.setId(senseId);
            sense.setLexicalEntryId(entryId);
            sense.setDefinition(value.definition().trim());
            sense.setShortDefinition(blankToNull(value.shortDefinition()));
            sense.setUsageLabel(value.usageLabel());
            sense.setDomainLabel(value.semanticDomain());
            sense.setDisplayOrder(value.displayOrder());
            sense.setStatus(PublicationStatus.DRAFT);
            sense.setUpdatedAt(now);
            sense.setUpdatedBy(actorId);
            if (isNew) {
                sense.setCreatedAt(now);
                sense.setCreatedBy(actorId);
            }
            senses.saveAndFlush(sense);
            if (!senseCitations.existsByOwnerIdAndCitationId(senseId, citationId)) {
                senseCitations.saveAndFlush(new SenseCitationEntity(senseId, citationId));
            }
        }
    }

    private void saveForms(UUID entryId, List<ValidatedForm> values, Instant now, UUID actorId) {
        for (ValidatedForm value : values) {
            LexicalFormEntity form = forms
                    .findFirstByLexicalEntryIdAndFormTypeAndNormalizedForm(entryId, value.type(), value.normalized())
                    .orElseGet(LexicalFormEntity::new);
            boolean isNew = form.getId() == null;
            if (isNew) {
                form.setId(stableId("lexical-form", entryId + ":" + value.type() + ":" + value.normalized()));
                form.setCreatedAt(now);
                form.setCreatedBy(actorId);
                form.setDisplayOrder(Math.max(1, forms.maxOrder(entryId) + 1));
            }
            form.setLexicalEntryId(entryId);
            form.setFormType(value.type());
            form.setOriginalForm(value.original());
            form.setNormalizedForm(value.normalized());
            form.setNotes("Imported from an explicitly attributed source; requires editorial review.");
            form.setStatus(PublicationStatus.DRAFT);
            form.setUpdatedAt(now);
            form.setUpdatedBy(actorId);
            forms.saveAndFlush(form);
        }
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

    private static void stamp(LexicalEntryEntity entry, Instant now, UUID actorId) {
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
        entry.setCreatedBy(actorId);
        entry.setUpdatedBy(actorId);
        entry.setVersion(0);
    }

    private static void stamp(LinguisticRootEntity root, Instant now, UUID actorId) {
        root.setCreatedAt(now);
        root.setUpdatedAt(now);
        root.setCreatedBy(actorId);
        root.setUpdatedBy(actorId);
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
        try (InputStream input = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (IOException exception) {
            throw new ContentImportException("Could not fingerprint content pack.", exception);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
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

    private static final class Analysis {
        private static final int MAX_RETAINED_ISSUES = 100;
        private static final int MAX_RETAINED_WARNINGS = 1000;
        private int recordsRead;
        private int valid;
        private int invalid;
        private int duplicates;
        private int newRecords;
        private int updates;
        private final List<String> warnings = new ArrayList<>();
        private final List<ImportIssue> issues = new ArrayList<>();

        int recordsRead() { return recordsRead; }
        void setRecordsRead(int recordsRead) { this.recordsRead = recordsRead; }
        int valid() { return valid; }
        void incrementValid() { valid++; }
        int invalid() { return invalid; }
        int duplicates() { return duplicates; }
        void incrementDuplicates() { duplicates++; }
        int newRecords() { return newRecords; }
        void incrementNewRecords() { newRecords++; }
        int updates() { return updates; }
        void incrementUpdates() { updates++; }
        List<String> warnings() { return List.copyOf(warnings); }
        List<ImportIssue> issues() { return List.copyOf(issues); }

        void addInvalid(List<ImportIssue> recordIssues) {
            invalid++;
            for (ImportIssue issue : recordIssues) addIssue(issue);
        }

        void addInvalid(ImportIssue issue) {
            invalid++;
            addIssue(issue);
        }

        void addWarnings(List<String> values) {
            for (String warning : values) addWarning(warning);
        }

        void addWarning(String warning) {
            if (warnings.size() < MAX_RETAINED_WARNINGS) warnings.add(warning);
        }

        private void addIssue(ImportIssue issue) {
            if (issues.size() < MAX_RETAINED_ISSUES) issues.add(issue);
        }
    }
}
