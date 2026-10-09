# Content population foundation

## Existing content model

The importer uses the current content tables and publication workflow; it does not create parallel domain records. Common editorial fields are `id`, `status`, `published_snapshot`, `reviewed_by`, `change_reason`, timestamps, actor IDs, and optimistic `version`. A public query must use the published snapshot/status rather than treating a draft row as public content.

| Domain | Entity / table | Required content and relationships | Publication | Source / provenance |
|---|---|---|---|---|
| Dictionary entry | `LexicalEntryEntity` / `lexical_entry` | Original and normalized lemma, part of speech; optional root, gender, vocalized form. Has senses, forms, examples and citation links. | Entry has editorial status/snapshot. Senses and forms have their own status. | `entry_citation`, `sense_citation`, `source_citation` → `reference_source`; import batch and record retain source ID, locator and hashes. |
| Root | `LinguisticRootEntity` / `linguistic_root` | Original/normalized root, radical count, slug; linked from entries through `root_id`. | Editorial workflow and published snapshot. | `root_citation` → `source_citation` → `reference_source`. |
| Meanings / senses | `LexicalSenseEntity` / `lexical_sense` | Entry ID, definition and display order; optional short definition, usage label and semantic domain. | Separate status from the entry; both must be published for public use. | `sense_citation` → `source_citation` → `reference_source`. |
| Morphology | `MorphologyAnalysisEntity` / `morphology_analysis`; patterns/rules in `morphological_pattern`, `morphology_rule` | Required lexical-entry ID, features and segmentation JSON; optional pattern, derivation, verb class and notes. | Analysis has its own status and published snapshot. | `morphology_analysis_citation` → source citations. The analysis must reference an existing lexical entry. |
| Grammar | `GrammarTopicEntity` / `grammar_topic`; `GrammarConceptEntity` / `grammar_concept`; `GrammarRuleEntity` / `grammar_rule`; examples, roles, annotations and relations in their corresponding tables | Topics group rules; concepts can alias and relate to rules; rules have components/examples and prerequisite/relation links. Key content includes normalized/original names, titles, definitions/rule text and stable slugs. | Topics and editorial records use the existing workflow/snapshot. | Topic, concept, rule and component citation-link tables point to `source_citation`. |
| Spelling | `SpellingTopicEntity` / `spelling_topic`; `SpellingRuleEntity` / `spelling_rule`; `spelling_clause`, `spelling_example` | Topic owns rules; rules own clauses/examples. Required titles/slugs and core rule; examples retain correct/incorrect forms and explanations. | Topic/rule editorial status and snapshots; child examples are tied to a rule. | Topic/rule citation links; an example may carry `citation_id`. |
| Rhetoric | `RhetoricTopicEntity` / `rhetoric_topic`; `RhetoricDeviceEntity` / `rhetoric_device`; components, examples and relations | Topic category/title; device topic ID, name, short definition and slug; examples contain text/explanation and interpretation fields. | Topic/device use editorial status and snapshots. | Topic/device citations and example citation IDs resolve through `source_citation`. |
| Literature | `LiteraryWorkEntity` / `literary_work`; figures, eras, genres, schools, aliases, roles, excerpts and link tables | Work requires title, language, slug and rights status; may link to genre/era/figures. Excerpts require work, text, citation and order. | Work and catalog entities use editorial status/snapshots. | Work/figure/era/genre/school citations; excerpts require a source citation. Work-level rights status, note and attribution are additional safeguards, not substitutes for source license review. |
| Articles | `ArticleEntity` / `article`; sections, tags, citations and knowledge relations | Article requires title, slug, excerpt and type; sections require article ID, heading, body and order. | Article editorial workflow/snapshot. | `article_citation` can point to the article or a section and resolves to `source_citation`. |
| Sources | `ReferenceSourceEntity` / `reference_source`; `SourceCitationEntity` / `source_citation` | Source requires type, title, license type, attribution, slug and identity key; citations attach locators/pages/labels to one source. | Source itself must be `PUBLISHED` before import resolution. | This is the provenance registry: URL, license, attribution, author/publisher/edition, notes and citation locators. |
| Learning | JDBC-managed `learning_path`, `learning_unit`, `learning_lesson`, objectives, sections, references, activities, quizzes/questions/options, attempts and revisions | Path groups ordered units/lessons, can depend on another path and publishes a versioned snapshot; children reference parent IDs. | Path workflow and snapshot; child content is included in that snapshot. | No general source-citation link is present in the learning schema; `learning_reference` points to an in-app target slug, not a bibliographic source. Do not import externally sourced lessons until provenance is represented. |
| Tools | No persistent content entity/table found for tool definitions | Tools are application behavior and consume existing published dictionary/morphology/grammar content as applicable. | Not an independent editorial publication model. | Provenance belongs to the referenced content records. |

`content_import_batch` and `content_import_record` are operational audit tables only. They retain batch/source/file hashes, import time and per-record source locator, hashes and target IDs; they are not alternate content tables.

## Import implementation and supported scope

The current implementation is intentionally an initial **dictionary lexical-seed adapter**, not a universal importer for every domain:

- `ContentPackReader` chooses JSON or CSV by file type. JSON requires `schemaVersion: 1`, `domain: "dictionary"`, and `records`; CSV requires the exact header in `CsvContentPackAdapter`.
- Validation uses the existing Arabic text normalizer. Display text remains unchanged; the stored search form folds diacritics/tatweel and the project's configured alef variants.
- Every record needs a stable key, source locator, Arabic lemma and part of speech, plus a definition or a source-backed root/plural.
- A source must already exist in the source registry, be `PUBLISHED`, use an allowed license, and have attribution. Import creates citations and leaves entries, senses and forms in `DRAFT`.
- Re-importing the same source record is idempotent. Content changes only update an existing imported `DRAFT`; published content is not overwritten.
- A non-dry-run with any invalid rows writes nothing. Writes are performed in a transaction and tracked by batch and record hashes. Dry-run reports counts without writing content or batch rows.
- Import runs only through the explicitly enabled command-line `ApplicationRunner`; it rejects a web application context and there is no public or admin HTTP import endpoint. Do not enable the property on the normal Render web service.
- The current reader deliberately caps packs at 25 MiB and materializes rows for validation/planning. This is suitable for the 500-row seed, but is **not yet certified for tens or hundreds of thousands of rows**. Move JSON/CSV ingestion, duplicate checks and report issue retention to bounded-memory streaming/batched persistence before raising that limit.

CSV column order:

```text
recordKey,sourceLocator,lemma,vocalizedForm,partOfSpeech,gender,root,rootNote,pluralForm,definition,shortDefinition,usageLabel,semanticDomain,pageFrom,pageTo
```

## First content pack and license decision

`content/dictionary/arabic-wordnet-v2-seed.json` is a 500-record derivative subset of Arabic WordNet data (AWN v2), pinned to OMW commit `406bf83b3c507a3d1f26e88252d5d66893fd36bf`. The upstream data header and `wns/arb/LICENSE` state CC BY-SA 3.0 and identify the authors. The license permits commercial reuse and modification subject to attribution and ShareAlike; this pack carries those obligations forward. Its source and selection metadata are in `content/sources/arabic-wordnet-v2.json`.

**Decision: APPROVED for this attributed CC BY-SA lexical subset**, based on the upstream license file and data header. Redistribution must retain attribution, link the license, identify modifications, and license the adapted dataset under CC BY-SA 3.0. This is a source-license check, not legal advice.

The source file provides lemmas, source-declared roots and broken plurals; it does **not** provide Arabic definitions, usage examples or a license-cleared Arabic sense inventory. The pack has no synthesized meanings, morphology analyses, or examples. Counts: 500 lexical records (497 distinct normalized spellings; the difference is cross-part-of-speech entries), 493 records with roots, 417 unique roots, 72 records with broken plurals, 0 senses, 0 published, 500 requiring editorial review. Source-ambiguous lemma/POS collisions were excluded. Dry-run should report no duplicate/invalid records and 423 informational normalization warnings (original display text is retained). All records remain drafts until the normal editorial workflow accepts them.

## Source registration and production import gate

Do not import to Production as part of this change. First register and publish the source using the existing source administration workflow; use the slug `arabic-wordnet-awn-v2`, type `DICTIONARY`, URL and license from the manifest, the complete author attribution there, and `CC_BY_SA`. Do not insert a source or content with ad-hoc SQL.

The one-off process must run outside the normal web service, with the existing production configuration supplied by the approved secret store. The `prod` profile retains `ProductionStartupGuard`; provide its valid production settings rather than bypassing it. Confirm the existing owner account is present before starting because the normal application bootstrap runner also executes.

Migration `V17__content_import_tracking.sql` is a required additive schema change. A normal Spring Boot start may run Flyway before the CLI runner, so the production procedure must first back up, then apply and verify V17 through the separately approved migration/release process. Only after V17 is present should the one-off CLI use `--spring.flyway.enabled=false`; this prevents a dry-run invocation from applying any pending schema migration. Do not run the CLI against a database that has not passed this precondition.

1. **Backup** from an approved host, using the existing [backup/restore runbook](./BACKUP_RESTORE.md). Its dump command is:

   ```text
   pg_dump --format=custom --no-owner --file=arabic-reference-YYYYMMDD.dump --dbname="$DATABASE_URL"
   ```

   Store credentials and the encrypted dump outside Git and outside public application disks. Do not proceed unless the command succeeds and the artifact is verified.

2. **Build the one-off artifact** after tests pass:

   ```powershell
   Set-Location backend
   .\mvnw.cmd -DskipTests package
   ```

3. **Dry run only** (set required `DB_*`, Redis and production application values in the process environment through the secret store; never paste secrets into the command or logs):

   ```powershell
   java -jar target\arabic-reference-0.1.0-SNAPSHOT.jar `
     --spring.profiles.active=prod `
     --spring.main.web-application-type=none `
     --spring.flyway.enabled=false `
     --content.import.enabled=true `
     --file=..\content\dictionary\arabic-wordnet-v2-seed.json `
     --source=arabic-wordnet-awn-v2 `
     --dry-run=true
   ```

   Review the JSON report. For an empty target, expected values are `recordsRead=500`, `valid=500`, `invalid=0`, `duplicates=0`, `newRecords=500`, `updates=0`, `committed=false`, with 423 normalization warnings. Any other result requires investigation; do not infer success from process startup.

4. **Import only after a separate explicit approval**, a second successful backup, and a reviewed dry-run report. Re-run the exact command with `--dry-run=false`; without `--dry-run`, the CLI defaults to write mode. Keep `--spring.flyway.enabled=false`.

5. **Verify counts and visibility** on the target database:

   ```sql
   SELECT status, count(*) FROM lexical_entry GROUP BY status ORDER BY status;
   SELECT count(*) FROM content_import_batch WHERE source_id = '<registered-source-uuid>';
   SELECT count(*) FROM content_import_record WHERE source_id = '<registered-source-uuid>';
   SELECT count(*) FROM lexical_entry e
     JOIN entry_citation ec ON ec.entry_id = e.id
     JOIN source_citation sc ON sc.id = ec.citation_id
     WHERE sc.source_id = '<registered-source-uuid>';
   ```

   Before editorial approval, imported rows must remain `DRAFT` and must not be visible from public dictionary/root/search/morphology endpoints. After the normal review/publish workflow, smoke-test `GET /api/v1/public/dictionary/by-slug/{slug}`, `GET /api/v1/public/dictionary/roots/{root}`, `GET /api/v1/public/search?q={word}`, and `GET /api/v1/public/morphology/analyze?word={word}`. Never publish all imported records as a batch merely to make the smoke test return data.

## Ten source-backed smoke records

These ten normalized lemmas are included in the pack and have a source-declared root and/or plural. Use their published slugs after review; do not expect a public response while they are drafts:

`كتاب`, `كاتب` (verb), `مكتوب`, `مكتبة`, `كتابة`, `شمس`, `قمر`, `باب`, `عين`, `مدرسة`.
