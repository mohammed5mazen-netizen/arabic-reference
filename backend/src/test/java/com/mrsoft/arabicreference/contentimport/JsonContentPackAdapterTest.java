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
