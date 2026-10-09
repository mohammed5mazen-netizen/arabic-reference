package com.mrsoft.arabicreference.contentimport.infrastructure;

import com.mrsoft.arabicreference.contentimport.domain.ContentPackFormatException;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord.ImportedForm;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord.ImportedSense;
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
                records.add(row.isObject() ? record(row) : emptyRecord());
            }
            return List.copyOf(records);
        } catch (ContentPackFormatException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ContentPackFormatException("Could not parse JSON content pack.", exception);
        }
    }

    public ImportedLexicalRecord readRecord(String json) {
        try {
            JsonNode row = mapper.readTree(json);
            if (row == null || !row.isObject()) {
                throw new ContentPackFormatException("Each NDJSON line must contain one dictionary record object.");
            }
            return record(row);
        } catch (ContentPackFormatException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ContentPackFormatException("Could not parse an NDJSON dictionary record.", exception);
        }
    }

    private static ImportedLexicalRecord record(JsonNode row) {
        return new ImportedLexicalRecord(
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
                text(row.path("pageTo")),
                senses(row.path("senses")),
                forms(row.path("forms")));
    }

    private static String text(JsonNode node) {
        return node.isString() ? node.asString() : null;
    }

    private static List<ImportedSense> senses(JsonNode node) {
        if (node.isMissingNode()) return List.of();
        if (!node.isArray()) throw new ContentPackFormatException("Record senses must be an array.");
        List<ImportedSense> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isObject()) throw new ContentPackFormatException("Each sense must be an object.");
            Integer order = item.path("displayOrder").canConvertToInt() ? item.path("displayOrder").intValue() : null;
            result.add(new ImportedSense(
                    text(item.path("definition")),
                    text(item.path("shortDefinition")),
                    text(item.path("usageLabel")),
                    text(item.path("semanticDomain")),
                    order));
        }
        return List.copyOf(result);
    }

    private static List<ImportedForm> forms(JsonNode node) {
        if (node.isMissingNode()) return List.of();
        if (!node.isArray()) throw new ContentPackFormatException("Record forms must be an array.");
        List<ImportedForm> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isObject()) throw new ContentPackFormatException("Each form must be an object.");
            result.add(new ImportedForm(
                    text(item.path("type")),
                    text(item.path("original")),
                    text(item.path("normalized"))));
        }
        return List.copyOf(result);
    }

    private static ImportedLexicalRecord emptyRecord() {
        return new ImportedLexicalRecord(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
