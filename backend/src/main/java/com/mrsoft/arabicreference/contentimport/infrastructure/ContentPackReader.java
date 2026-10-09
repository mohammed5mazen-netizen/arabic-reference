package com.mrsoft.arabicreference.contentimport.infrastructure;

import com.mrsoft.arabicreference.contentimport.domain.ContentPackFormatException;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class ContentPackReader {
    private static final long MAX_PACK_BYTES = 25L * 1024 * 1024;

    private final JsonContentPackAdapter json;
    private final NdjsonContentPackAdapter ndjson;
    private final CsvContentPackAdapter csv;

    public ContentPackReader(JsonContentPackAdapter json, NdjsonContentPackAdapter ndjson, CsvContentPackAdapter csv) {
        this.json = json;
        this.ndjson = ndjson;
        this.csv = csv;
    }

    public List<ImportedLexicalRecord> read(Path path) {
        List<ImportedLexicalRecord> records = new ArrayList<>();
        forEachRecord(path, records::add);
        return List.copyOf(records);
    }

    public int forEachRecord(Path path, Consumer<ImportedLexicalRecord> consumer) {
        try {
            if (!Files.isRegularFile(path) || Files.size(path) > MAX_PACK_BYTES) {
                throw new ContentPackFormatException("Content pack must be a regular file no larger than 25 MiB.");
            }
            String fileName = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
            if (fileName.endsWith(".ndjson")) {
                return ndjson.forEach(path, consumer);
            }
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (fileName.endsWith(".json")) {
                List<ImportedLexicalRecord> records = json.read(content);
                records.forEach(consumer);
                return records.size();
            }
            if (fileName.endsWith(".csv")) {
                List<ImportedLexicalRecord> records = csv.read(content);
                records.forEach(consumer);
                return records.size();
            }
            throw new ContentPackFormatException("Content pack extension must be .json, .ndjson, or .csv.");
        } catch (ContentPackFormatException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ContentPackFormatException("Could not read content pack.", exception);
        }
    }
}
