package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.contentimport.domain.ContentPackFormatException;
import com.mrsoft.arabicreference.contentimport.infrastructure.JsonContentPackAdapter;
import com.mrsoft.arabicreference.contentimport.infrastructure.NdjsonContentPackAdapter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class NdjsonContentPackAdapterTest {
    @TempDir Path directory;

    @Test
    void readsEntryObjectsOnePerLineAndSkipsBlankLines() throws Exception {
        Path file = directory.resolve("entries.ndjson");
        Files.writeString(file, """
                {"recordKey":"one","sourceLocator":"urn:test:one","lemma":"كتاب","partOfSpeech":"NOUN","senses":[{"definition":"تعريف","displayOrder":1}]}

                {"recordKey":"two","sourceLocator":"urn:test:two","lemma":"لغة","partOfSpeech":"NOUN","root":"لغي","rootNote":"test source root"}
                """);
        var adapter = new NdjsonContentPackAdapter(new JsonContentPackAdapter(JsonMapper.builder().build()));

        assertThat(adapter.read(file)).hasSize(2);
    }

    @Test
    void reportsMalformedLineNumber() throws Exception {
        Path file = directory.resolve("malformed.ndjson");
        Files.writeString(file, "{\"recordKey\":\"one\"}\n{\n");
        var adapter = new NdjsonContentPackAdapter(new JsonContentPackAdapter(JsonMapper.builder().build()));

        assertThatThrownBy(() -> adapter.read(file))
                .isInstanceOf(ContentPackFormatException.class)
                .hasMessageContaining("line 2");
    }
}
