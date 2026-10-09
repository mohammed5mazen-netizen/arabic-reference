package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mrsoft.arabicreference.IntegrationContainers;
import com.mrsoft.arabicreference.contentimport.application.ContentImportException;
import com.mrsoft.arabicreference.contentimport.application.ContentImportService;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import com.mrsoft.arabicreference.dictionary.domain.PartOfSpeech;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.source.domain.LicenseType;
import com.mrsoft.arabicreference.source.domain.SourceType;
import com.mrsoft.arabicreference.source.infrastructure.persistence.ReferenceSourceEntity;
import com.mrsoft.arabicreference.source.infrastructure.persistence.ReferenceSourceRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ContentImportIntegrationTest {
    private static final UUID TEST_ACTOR = UUID.fromString("00000000-0000-4000-8000-0000000000aa");
    private static final UUID IMPORT_ACTOR = UUID.fromString("00000000-0000-4000-8000-0000000000ab");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("content.import.batch-size", () -> "1");
    }

    @Autowired private ContentImportService imports;
    @Autowired private ReferenceSourceRepository sources;
    @Autowired private LexicalEntryRepository entries;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper mapper;
    @Autowired private MockMvc mockMvc;

    @TempDir Path tempDir;
    private ReferenceSourceEntity source;

    @BeforeEach
    void createApprovedTestSource() {
        jdbc.update("""
                insert into admin_user (
                    id, username, email, display_name, password_hash, status, must_change_password,
                    failed_login_attempts, password_changed_at, created_at, updated_at, version
                ) values (?, 'content.importer', 'content-importer@example.test', 'Content Importer',
                    'test-disabled-import-actor-hash', 'DISABLED', true, 0, now(), now(), now(), 0)
                """, IMPORT_ACTOR);
        Instant now = Instant.now();
        source = new ReferenceSourceEntity();
        source.setId(UUID.randomUUID());
        source.setSourceType(SourceType.DICTIONARY);
        source.setTitle("Test-only content source");
        source.setUrl("https://example.test/test-only");
        source.setLicenseType(LicenseType.CC0);
        source.setPublicDomain(false);
        source.setAttributionText("Test fixture only; not production content.");
        source.setStatus(PublicationStatus.PUBLISHED);
        source.setSlug("content-import-" + UUID.randomUUID().toString().replace("-", ""));
        source.setIdentityKey(source.getSlug());
        source.setCreatedAt(now);
        source.setUpdatedAt(now);
        source.setCreatedBy(TEST_ACTOR);
        source.setUpdatedBy(TEST_ACTOR);
        source.setVersion(0);
        source = sources.saveAndFlush(source);
    }

    @AfterEach
    void removeFixtureData() {
        UUID sourceId = source.getId();
        jdbc.update("delete from content_import_record where source_id = ?", sourceId);
        jdbc.update("delete from entry_citation where entry_id in (select id from lexical_entry where created_by = ?)", IMPORT_ACTOR);
        jdbc.update("delete from sense_citation where sense_id in (select id from lexical_sense where created_by = ?)", IMPORT_ACTOR);
        jdbc.update("delete from root_citation where root_id in (select id from linguistic_root where created_by = ?)", IMPORT_ACTOR);
        jdbc.update("delete from lexical_form where created_by = ?", IMPORT_ACTOR);
        jdbc.update("delete from lexical_sense where created_by = ?", IMPORT_ACTOR);
        jdbc.update("delete from lexical_entry where created_by = ?", IMPORT_ACTOR);
        jdbc.update("delete from linguistic_root where created_by = ?", IMPORT_ACTOR);
        jdbc.update("delete from source_citation where source_id = ?", sourceId);
        jdbc.update("delete from content_import_batch where source_id = ?", sourceId);
        sources.deleteById(sourceId);
        jdbc.update("delete from admin_user where id = ?", IMPORT_ACTOR);
    }

    @Test
    void importsDraftsWithProvenanceAndReimportDoesNotDuplicate() throws Exception {
        Path file = writePack("record-1", "كتاب", "كتب", "كُتُب", "Meaning supported by the test source.");

        var first = imports.importPack(file, source.getSlug(), false);
        assertThat(first.committed()).isTrue();
        assertThat(first.newRecords()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from lexical_entry where created_by = ?", Integer.class, IMPORT_ACTOR)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from lexical_sense where created_by = ?", Integer.class, IMPORT_ACTOR)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from lexical_form where created_by = ?", Integer.class, IMPORT_ACTOR)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from content_import_record where source_id = ?", Integer.class, source.getId())).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from entry_citation ec join lexical_entry e on e.id = ec.entry_id where e.created_by = ?", Integer.class, IMPORT_ACTOR)).isEqualTo(1);

        var entry = entries.findByLemmaNormalizedAndPartOfSpeech("كتاب", PartOfSpeech.NOUN).orElseThrow();
        assertThat(entry.getStatus()).isEqualTo(PublicationStatus.DRAFT);
        assertThat(entry.getPublishedSnapshot()).isNull();
        assertThat(entry.getRootId()).isNotNull();
        mockMvc.perform(get("/api/v1/public/dictionary/by-slug/" + entry.getSlug()))
                .andExpect(status().isNotFound());

        var second = imports.importPack(file, source.getSlug(), false);
        assertThat(second.committed()).isTrue();
        assertThat(second.duplicates()).isEqualTo(1);
        assertThat(second.newRecords()).isZero();
        assertThat(jdbc.queryForObject("select count(*) from lexical_entry where created_by = ?", Integer.class, IMPORT_ACTOR)).isEqualTo(1);
    }

    @Test
    void dryRunAndInvalidPacksDoNotMutateDatabase() throws Exception {
        Path dryRunFile = writePack("dry-run-1", "المرسوم", null, null, "محتوى تحضيري للاختبار فقط.");
        var dryRun = imports.importPack(dryRunFile, source.getSlug(), true);
        assertThat(dryRun.valid()).isEqualTo(1);
        assertThat(dryRun.newRecords()).isEqualTo(1);
        assertThat(dryRun.committed()).isFalse();
        assertThat(jdbc.queryForObject("select count(*) from content_import_batch where source_id = ?", Integer.class, source.getId())).isZero();
        assertThat(entries.findByLemmaNormalizedAndPartOfSpeech("المرسوم", PartOfSpeech.NOUN)).isEmpty();

        Path invalidFile = writePack("invalid-1", "المثال", null, null, null, null);
        var invalid = imports.importPack(invalidFile, source.getSlug(), false);
        assertThat(invalid.invalid()).isGreaterThan(0);
        assertThat(invalid.committed()).isFalse();
        assertThat(jdbc.queryForObject("select count(*) from content_import_batch where source_id = ?", Integer.class, source.getId())).isZero();
    }

    @Test
    void refusesUnapprovedLicenseAndRequiresAnExplicitSource() throws Exception {
        source.setLicenseType(LicenseType.RESTRICTED);
        sources.saveAndFlush(source);
        Path file = writePack("restricted-1", "نافذة", null, null, "مادة اختبارية.");

        assertThatThrownBy(() -> imports.importPack(file, source.getSlug(), true))
                .isInstanceOf(ContentImportException.class)
                .hasMessageContaining("license");
        assertThatThrownBy(() -> imports.importPack(file, " ", true))
                .isInstanceOf(ContentImportException.class)
                .hasMessageContaining("source slug");
    }

    @Test
    void transactionRollsBackTheWholeBatchWhenAWriteFails() throws Exception {
        Path file = writePack("rollback-1", "المكتبة", "كتب", "مكتبات", "مادة اختبارية.");
        jdbc.execute("""
                create function fail_content_import_record_for_test() returns trigger language plpgsql as $$
                begin
                    raise exception 'forced test rollback';
                end
                $$
                """);
        jdbc.execute("""
                create trigger fail_content_import_record_for_test
                before insert on content_import_record
                for each row execute function fail_content_import_record_for_test()
                """);
        try {
            assertThatThrownBy(() -> imports.importPack(file, source.getSlug(), false))
                    .isInstanceOf(RuntimeException.class);
            assertThat(jdbc.queryForObject("select count(*) from content_import_batch where source_id = ?", Integer.class, source.getId())).isZero();
            assertThat(jdbc.queryForObject("select count(*) from lexical_entry where lemma_normalized = 'المكتبة'", Integer.class)).isZero();
            assertThat(jdbc.queryForObject("select count(*) from source_citation where source_id = ?", Integer.class, source.getId())).isZero();
        } finally {
            jdbc.execute("drop trigger if exists fail_content_import_record_for_test on content_import_record");
            jdbc.execute("drop function if exists fail_content_import_record_for_test()");
        }
    }

    @Test
    void rerunningPackResumesAfterAlreadyCommittedBatches() throws Exception {
        Path file = writeMultiPack();
        jdbc.execute("""
                create function fail_second_content_import_record_for_test() returns trigger language plpgsql as $$
                begin
                    if new.source_record_key = 'resume-2' then
                        raise exception 'forced second batch failure';
                    end if;
                    return new;
                end
                $$
                """);
        jdbc.execute("""
                create trigger fail_second_content_import_record_for_test
                before insert on content_import_record
                for each row execute function fail_second_content_import_record_for_test()
                """);
        try {
            assertThatThrownBy(() -> imports.importPack(file, source.getSlug(), false))
                    .isInstanceOf(RuntimeException.class);
            assertThat(jdbc.queryForObject("select count(*) from lexical_entry where lemma_normalized in ('كتاب', 'كاتب')", Integer.class))
                    .isEqualTo(1);
        } finally {
            jdbc.execute("drop trigger if exists fail_second_content_import_record_for_test on content_import_record");
            jdbc.execute("drop function if exists fail_second_content_import_record_for_test()");
        }

        var resumed = imports.importPack(file, source.getSlug(), false);
        assertThat(resumed.committed()).isTrue();
        assertThat(resumed.duplicates()).isEqualTo(1);
        assertThat(resumed.newRecords()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from lexical_entry where lemma_normalized in ('كتاب', 'كاتب')", Integer.class))
                .isEqualTo(2);
    }

    @Test
    void importsMultipleSensesAndFormsWithoutDuplicates() throws Exception {
        var row = new ImportedLexicalRecord(
                "nested-record", "https://example.test/nested-record", "كتاب", null, "NOUN", null, null, null,
                null, null, null, null, null, null, null,
                java.util.List.of(
                        new ImportedLexicalRecord.ImportedSense("معنى أول موثق.", null, null, "GENERAL", 1),
                        new ImportedLexicalRecord.ImportedSense("معنى ثان موثق.", null, null, "LANGUAGE", 2)),
                java.util.List.of(
                        new ImportedLexicalRecord.ImportedForm("PLURAL", "كتب", "كتب"),
                        new ImportedLexicalRecord.ImportedForm("ALTERNATE", "كِتاب", "كتاب")));
        Path file = tempDir.resolve("nested.json");
        Files.writeString(file, mapper.writeValueAsString(java.util.Map.of(
                "schemaVersion", 1, "domain", "dictionary", "records", java.util.List.of(row))));

        var report = imports.importPack(file, source.getSlug(), false);
        assertThat(report.committed()).isTrue();
        assertThat(jdbc.queryForObject("select count(*) from lexical_sense where lexical_entry_id = ?", Integer.class,
                jdbc.queryForObject("select id from lexical_entry where lemma_normalized = 'كتاب' and part_of_speech = 'NOUN'", UUID.class)))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from lexical_form where lexical_entry_id = ?", Integer.class,
                jdbc.queryForObject("select id from lexical_entry where lemma_normalized = 'كتاب' and part_of_speech = 'NOUN'", UUID.class)))
                .isEqualTo(2);
    }

    private Path writePack(String recordKey, String lemma, String root, String plural, String definition) throws Exception {
        return writePack(recordKey, lemma, root, plural, definition, "https://example.test/" + recordKey);
    }

    private Path writePack(String recordKey, String lemma, String root, String plural, String definition, String sourceLocator) throws Exception {
        var row = new ImportedLexicalRecord(
                recordKey, sourceLocator, lemma, null, "NOUN", null, root, null, plural,
                definition, null, null, null, null, null);
        Path file = tempDir.resolve(recordKey + ".json");
        Files.writeString(file, mapper.writeValueAsString(java.util.Map.of(
                "schemaVersion", 1,
                "domain", "dictionary",
                "records", java.util.List.of(row))));
        return file;
    }

    private Path writeMultiPack() throws Exception {
        var first = new ImportedLexicalRecord(
                "resume-1", "https://example.test/resume-1", "كتاب", null, "NOUN", null, "كتب", null, "كتب",
                "معنى اختباري موثق.", null, null, null, null, null);
        var second = new ImportedLexicalRecord(
                "resume-2", "https://example.test/resume-2", "كاتب", null, "NOUN", null, "كتب", null, null,
                "معنى اختباري موثق.", null, null, null, null, null);
        Path file = tempDir.resolve("resume.json");
        Files.writeString(file, mapper.writeValueAsString(java.util.Map.of(
                "schemaVersion", 1, "domain", "dictionary", "records", java.util.List.of(first, second))));
        return file;
    }

}
