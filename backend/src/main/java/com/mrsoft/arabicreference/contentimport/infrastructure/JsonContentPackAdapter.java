package com.mrsoft.arabicreference.contentimport.infrastructure;

import com.mrsoft.arabicreference.contentimport.domain.ContentPackFormatException;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class JsonContentPackAdapter {
    private final JsonMapper mapper;

    public JsonContentPackAdapter(JsonMapper mapper) {
        this.mapper = mapper;
    }

    public List<ImportedLexicalRecord> read(String json) {
        try {
            JsonNode document = mapper.readTree(json);
            if (document == null || !document.isObject()
                    || !document.path("schemaVersion").canConvertToInt()
                    || document.path("schemaVersion").intValue() != 1
                    || !"dictionary".equals(text(document.path("domain")))
                    || !document.path("records").isArray()) {
                throw new ContentPackFormatException("JSON pack must have schemaVersion 1, domain 'dictionary', and a records array.");
            }
            List<ImportedLexicalRecord> records = new ArrayList<>();
            for (JsonNode row : document.path("records")) {
                if (!row.isObject()) {
                    records.add(emptyRecord());
                    continue;
                }
                records.add(new ImportedLexicalRecord(
                        text(row.path("recordKey")),
                        text(row.path("sourceLocator")),
                        text(row.path("lemma")),
                        text(row.path("vocalizedForm")),
                        text(row.path("partOfSpeech")),
                        text(row.path("gender")),
                        text(row.path("root")),
                        text(row.path("rootNote")),
                        text(row.path("pluralForm")),
                        text(row.path("definition")),
                        text(row.path("shortDefinition")),
                        text(row.path("usageLabel")),
                        text(row.path("semanticDomain")),
                        text(row.path("pageFrom")),
                        text(row.path("pageTo"))));
            }
            return List.copyOf(records);
        } catch (ContentPackFormatException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ContentPackFormatException("Could not parse JSON content pack.", exception);
        }
    }

    private static String text(JsonNode node) {
        return node.isString() ? node.asString() : null;
    }

    private static ImportedLexicalRecord emptyRecord() {
        return new ImportedLexicalRecord(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
