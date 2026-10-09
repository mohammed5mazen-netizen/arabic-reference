package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.contentimport.domain.ContentPackFormatException;
import com.mrsoft.arabicreference.contentimport.infrastructure.CsvContentPackAdapter;
import org.junit.jupiter.api.Test;

class CsvContentPackAdapterTest {
    private final CsvContentPackAdapter adapter = new CsvContentPackAdapter();
    private static final String HEADER = "recordKey,sourceLocator,lemma,vocalizedForm,partOfSpeech,gender,root,rootNote,pluralForm,definition,shortDefinition,usageLabel,semanticDomain,pageFrom,pageTo";

    @Test
    void readsQuotedCommasEscapedQuotesAndMultilineFields() {
        String csv = HEADER + "\r\n"
                + "rec-1,https://example.test/1,كتاب,,NOUN,,كتب,,كتب,\"تعريف، \"\"أصلي\"\"\n"
                + "ممتد\",,,,,";

        var records = adapter.read(csv);

        assertThat(records).hasSize(1);
        assertThat(records.getFirst().lemma()).isEqualTo("كتاب");
        assertThat(records.getFirst().definition()).isEqualTo("تعريف، \"أصلي\"\nممتد");
        assertThat(records.getFirst().pluralForm()).isEqualTo("كتب");
    }

    @Test
    void rejectsMalformedCsvAndUnsupportedHeaders() {
        assertThatThrownBy(() -> adapter.read(HEADER + "\nrec-1,\"unterminated"))
                .isInstanceOf(ContentPackFormatException.class)
                .hasMessageContaining("unterminated");
        assertThatThrownBy(() -> adapter.read("lemma,definition\nكتاب,معنى"))
                .isInstanceOf(ContentPackFormatException.class)
                .hasMessageContaining("header");
        assertThatThrownBy(() -> adapter.read(HEADER + "\nrec-1,too,few"))
                .isInstanceOf(ContentPackFormatException.class)
                .hasMessageContaining("expected");
    }
}
