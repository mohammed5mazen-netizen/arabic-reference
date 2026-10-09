# Content import operations

## Supported scope

The current importer writes lexical entries, associated roots, senses, and forms through the existing dictionary entities and citation tables. JSON and CSV are supported for smaller packs. NDJSON is the preferred format for larger packs; one line represents one dictionary entry record, including any `senses` and `forms` arrays.

Imported entries, roots, senses, and forms are always `DRAFT`. The importer does not construct or publish `published_snapshot` values. The normal dictionary editorial workflow remains responsible for review and publication. Publishing dictionary entries invokes the existing incremental dictionary search indexer; draft import does not add private records to the public index. The existing admin search-rebuild operation remains available for index verification/repair.

This is not yet a general importer for grammar, spelling, rhetoric, literature, articles, learning, or morphology. Do not encode those domains as dictionary entries.

## Source and license gate

Before running a pack, register and publish its `reference_source` through the existing source workflow. The source needs a supported license and explicit attribution. `UNKNOWN` and `RESTRICTED` sources are rejected. The importer does not create source records or infer licenses.

Each pack record needs a stable `recordKey` and `sourceLocator`; the source registry slug is passed separately. The stored entry, sense, form, and root citations point to this registered source. The importer obtains audit IDs only from the existing `content.importer` account and fails if it is missing or not `DISABLED`. It never creates an account or changes account status.

## Formats

Versioned JSON uses `schemaVersion: 1`, `domain: "dictionary"`, and a `records` array. Existing flat `definition` and `pluralForm` fields remain supported. Multiple senses and forms use:

```json
{
  "recordKey": "source-record-1",
  "sourceLocator": "https://source.example/record/1",
  "lemma": "كتاب",
  "partOfSpeech": "NOUN",
  "root": "كتب",
  "senses": [
    { "definition": "تعريف موثق من المصدر.", "semanticDomain": "GENERAL", "displayOrder": 1 }
  ],
  "forms": [
    { "type": "PLURAL", "original": "كتب", "normalized": "كتب" }
  ]
}
```

An NDJSON file contains one such record object per non-empty line. JSON and CSV currently materialize their contents during parsing; prefer NDJSON for larger imports. The reader currently caps pack files at 25 MiB.

CSV uses the fixed header/column order defined in `CsvContentPackAdapter`. The CSV layout covers the legacy one-sense/one-plural entry form; use JSON or NDJSON for multiple senses and forms.

## Dry run

Use a separate one-off process, never the ordinary web service. Dry run is the CLI default; writing requires the explicit `--dry-run=false` option. Ensure schema migrations are already applied and configure the database/Redis connection through the approved local secret store. Do not put credentials in command arguments or pack files.

```powershell
Set-Location backend
.\mvnw.cmd -DskipTests package

java -jar target\arabic-reference-0.1.0-SNAPSHOT.jar `
  --spring.profiles.active=prod `
  --spring.main.web-application-type=none `
  --spring.flyway.enabled=false `
  --content.import.enabled=true `
  --content.import.batch-size=500 `
  --file=..\content\dictionary\arabic-wordnet-v2-seed.json `
  --source=arabic-wordnet-awn-v2 `
  --dry-run=true
```

Review the complete JSON report. Dry-run performs validation and duplicate checks, but writes no content, citation, import-batch, or checkpoint rows. A report with any invalid record must not be executed.

## Execute and resume

Only after a successful dry-run, a verified backup, and explicit operational approval, use the same command with `--dry-run=false`. If the process stops after one or more committed chunks, rerun the exact same immutable pack with the same source slug. Existing source record keys and content hashes are checked to skip committed rows; uncommitted work is retried. Do not edit the file during a run or change the source mapping between attempts.

Each chunk commits in its own database transaction. The default chunk size is 500; configure `--content.import.batch-size` between 1 and 5000 to override. A failed chunk rolls back without rolling back earlier committed chunks. Resume is replay-based rather than offset-based; the import record keys and hashes are the checkpoint.

## Rollback and publication

Do not delete or truncate content tables to undo an import. Imported items remain drafts and can be reviewed or archived through the normal editorial workflow. For operational recovery, follow [BACKUP_RESTORE.md](./BACKUP_RESTORE.md), restore into a separate database, and verify before changing any production data. There is no automatic destructive rollback command.

## Search index

Draft content must not be indexed as public. Publishing a dictionary entry uses the existing incremental dictionary indexer. After an approved publication batch, use the existing admin search status/rebuild tools if verification shows the index is inconsistent. The import runner does not bypass the admin permission boundary or invoke a public rebuild endpoint.

## Production restrictions

Never enable `content.import.enabled` on the persistent Render web service. The runner refuses a web application context and no public import endpoint exists. The repository's current pack is a source-backed draft seed, not a claim that meanings or morphology are supplied by that source. Packs for other domains and licensed sense content require a separate adapter and a verified source.
