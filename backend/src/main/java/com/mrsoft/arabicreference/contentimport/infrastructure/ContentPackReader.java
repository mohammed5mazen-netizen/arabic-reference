package com.mrsoft.arabicreference.contentimport.infrastructure;

import com.mrsoft.arabicreference.contentimport.domain.ContentPackFormatException;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ContentPackReader {
    private static final long MAX_PACK_BYTES = 25L * 1024 * 1024;

    private final JsonContentPackAdapter json;
    private final CsvContentPackAdapter csv;

    public ContentPackReader(JsonContentPackAdapter json, CsvContentPackAdapter csv) {
        this.json = json;
        this.csv = csv;
    }

    public List<ImportedLexicalRecord> read(Path path) {
        try {
            if (!Files.isRegularFile(path) || Files.size(path) > MAX_PACK_BYTES) {
                throw new ContentPackFormatException("Content pack must be a regular file no larger than 25 MiB.");
            }
            String content = Files.readString(path, StandardCharsets.UTF_8);
            String fileName = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
            if (fileName.endsWith(".json")) {
                return json.read(content);
            }
            if (fileName.endsWith(".csv")) {
                return csv.read(content);
            }
            throw new ContentPackFormatException("Content pack extension must be .json or .csv.");
        } catch (ContentPackFormatException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ContentPackFormatException("Could not read content pack.", exception);
        }
    }
}
