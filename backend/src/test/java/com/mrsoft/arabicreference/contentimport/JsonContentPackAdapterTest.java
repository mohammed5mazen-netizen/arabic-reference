package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.contentimport.domain.ContentPackFormatException;
import com.mrsoft.arabicreference.contentimport.infrastructure.JsonContentPackAdapter;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

class JsonContentPackAdapterTest {
    private final JsonContentPackAdapter adapter = new JsonContentPackAdapter(JsonMapper.builder().build());

    @Test
    void readsVersionedDictionaryPack() {
        var records = adapter.read("""
                {"schemaVersion":1,"domain":"dictionary","records":[
                  {"recordKey":"ar-1","sourceLocator":"https://example.test/1","lemma":"كلمة","partOfSpeech":"NOUN","definition":"تعريف"}
                ]}
                """);

        assertThat(records).hasSize(1);
        assertThat(records.getFirst().lemma()).isEqualTo("كلمة");
        assertThat(records.getFirst().definition()).isEqualTo("تعريف");
    }

    @Test
    void readsNestedSensesAndTypedForms() {
        var record = adapter.read("""
                {"schemaVersion":1,"domain":"dictionary","records":[
                  {"recordKey":"ar-2","sourceLocator":"urn:test:2","lemma":"كتاب","partOfSpeech":"NOUN",
                   "senses":[{"definition":"معنى أول","domain":"GENERAL","displayOrder":1},
                             {"definition":"معنى ثان","domain":"LANGUAGE","displayOrder":2}],
                   "forms":[{"type":"PLURAL","original":"كتب","normalized":"كتب"},
                            {"type":"FEMININE","original":"كتابة","normalized":"كتابة"}]}
                ]}
                """).getFirst();

        assertThat(record.senses()).hasSize(2);
        assertThat(record.senses().get(1).definition()).isEqualTo("معنى ثان");
        assertThat(record.forms()).extracting("type").containsExactly("PLURAL", "FEMININE");
    }

    @Test
    void rejectsMalformedOrWrongSchemaPacks() {
        assertThatThrownBy(() -> adapter.read("{"))
                .isInstanceOf(ContentPackFormatException.class)
                .hasMessageContaining("parse JSON");
        assertThatThrownBy(() -> adapter.read("""
                {"schemaVersion":2,"domain":"dictionary","records":[]}
                """))
                .isInstanceOf(ContentPackFormatException.class)
                .hasMessageContaining("schemaVersion");
    }
}
