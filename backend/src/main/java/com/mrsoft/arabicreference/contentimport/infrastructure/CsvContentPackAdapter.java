package com.mrsoft.arabicreference.contentimport.infrastructure;

import com.mrsoft.arabicreference.contentimport.domain.ContentPackFormatException;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CsvContentPackAdapter {
    private static final List<String> COLUMNS = List.of(
            "recordKey", "sourceLocator", "lemma", "vocalizedForm", "partOfSpeech", "gender", "root", "rootNote",
            "pluralForm", "definition", "shortDefinition", "usageLabel", "semanticDomain", "pageFrom", "pageTo");

    public List<ImportedLexicalRecord> read(String csv) {
        List<List<String>> rows = parseRows(csv);
        if (rows.isEmpty()) {
            throw new ContentPackFormatException("CSV content pack is empty.");
        }
        List<String> header = rows.getFirst().stream().map(String::trim).toList();
        if (!header.equals(COLUMNS)) {
            throw new ContentPackFormatException("CSV header does not match the supported dictionary content-pack schema.");
        }
        List<ImportedLexicalRecord> records = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            List<String> row = rows.get(rowIndex);
            if (row.size() != COLUMNS.size()) {
                throw new ContentPackFormatException("CSV row " + (rowIndex + 1) + " has " + row.size() + " fields; expected " + COLUMNS.size() + ".");
            }
            Map<String, String> values = new HashMap<>();
            for (int column = 0; column < COLUMNS.size(); column++) {
                values.put(COLUMNS.get(column), row.get(column));
            }
            records.add(new ImportedLexicalRecord(
                    values.get("recordKey"), values.get("sourceLocator"), values.get("lemma"), values.get("vocalizedForm"),
                    values.get("partOfSpeech"), values.get("gender"), values.get("root"), values.get("rootNote"), values.get("pluralForm"),
                    values.get("definition"), values.get("shortDefinition"), values.get("usageLabel"),
                    values.get("semanticDomain"), values.get("pageFrom"), values.get("pageTo")));
        }
        return List.copyOf(records);
    }

    private static List<List<String>> parseRows(String csv) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;
        for (int index = 0; index < csv.length(); index++) {
            char current = csv.charAt(index);
            if (quoted) {
                if (current == '"' && index + 1 < csv.length() && csv.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else if (current == '"') {
                    quoted = false;
                    closedQuote = true;
                } else {
                    field.append(current);
                }
                continue;
            }
            if (closedQuote && current != ',' && current != '\r' && current != '\n') {
                throw new ContentPackFormatException("Unexpected character after a quoted CSV field.");
            }
            if (current == '"' && field.isEmpty() && !closedQuote) {
                quoted = true;
            } else if (current == ',') {
                addField(row, field);
                closedQuote = false;
            } else if (current == '\r' || current == '\n') {
                if (current == '\r' && index + 1 < csv.length() && csv.charAt(index + 1) == '\n') {
                    index++;
                }
                addField(row, field);
                if (!(row.size() == 1 && row.getFirst().isEmpty())) {
                    rows.add(List.copyOf(row));
                }
                row.clear();
                closedQuote = false;
            } else {
                field.append(current);
            }
        }
        if (quoted) {
            throw new ContentPackFormatException("CSV contains an unterminated quoted field.");
        }
        if (!field.isEmpty() || !row.isEmpty() || closedQuote) {
            addField(row, field);
            if (!(row.size() == 1 && row.getFirst().isEmpty())) {
                rows.add(List.copyOf(row));
            }
        }
        return rows;
    }

    private static void addField(List<String> row, StringBuilder field) {
        row.add(field.toString());
        field.setLength(0);
    }
}
