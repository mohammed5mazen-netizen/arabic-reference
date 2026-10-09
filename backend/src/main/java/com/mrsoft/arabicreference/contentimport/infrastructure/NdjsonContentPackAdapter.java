package com.mrsoft.arabicreference.contentimport.infrastructure;

import com.mrsoft.arabicreference.contentimport.domain.ContentPackFormatException;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class NdjsonContentPackAdapter {
    private final JsonContentPackAdapter jsonRecords;

    public NdjsonContentPackAdapter(JsonContentPackAdapter jsonRecords) {
        this.jsonRecords = jsonRecords;
    }

    public List<ImportedLexicalRecord> read(Path path) {
        List<ImportedLexicalRecord> records = new ArrayList<>();
        forEach(path, records::add);
        return List.copyOf(records);
    }

    public int forEach(Path path, Consumer<ImportedLexicalRecord> consumer) {
        int records = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                try {
                    consumer.accept(jsonRecords.readRecord(line));
                    records++;
                } catch (ContentPackFormatException exception) {
                    throw new ContentPackFormatException("Invalid NDJSON record on line " + lineNumber + ".", exception);
                }
            }
        } catch (ContentPackFormatException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ContentPackFormatException("Could not read NDJSON content pack.", exception);
        }
        if (records == 0) throw new ContentPackFormatException("NDJSON content pack contains no records.");
        return records;
    }
}
