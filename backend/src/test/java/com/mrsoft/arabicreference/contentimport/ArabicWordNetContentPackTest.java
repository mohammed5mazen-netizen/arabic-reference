package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.contentimport.application.ImportValidator;
import com.mrsoft.arabicreference.contentimport.application.NormalizationService;
import com.mrsoft.arabicreference.contentimport.infrastructure.JsonContentPackAdapter;
import com.mrsoft.arabicreference.linguistics.application.ArabicTextNormalizationService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ArabicWordNetContentPackTest {
    private static final Path PACK = Path.of("..", "content", "dictionary", "arabic-wordnet-v2-seed.json");
    private static final Path MANIFEST = Path.of("..", "content", "sources", "arabic-wordnet-v2.json");

    @Test
    void productionPackContainsOnlyValidSourceBackedRowsAndKnownSamples() throws Exception {
        var adapter = new JsonContentPackAdapter(JsonMapper.builder().build());
        var validator = new ImportValidator(new NormalizationService(new ArabicTextNormalizationService()));
        var records = adapter.read(Files.readString(PACK));

        assertThat(records).hasSize(500);
        assertThat(records.stream().map(record -> record.recordKey()).collect(Collectors.toSet())).hasSize(500);
        assertThat(records.stream()
                .map(record -> validator.validate(record, 1).record().normalizedLemma() + "|" + record.partOfSpeech())
                .collect(Collectors.toSet())).hasSize(500);
        assertThat(records.stream()
                .mapToInt(record -> validator.validate(record, 1).warnings().size())
                .sum()).isEqualTo(423);
        assertThat(records).allSatisfy(record -> {
            assertThat(record.sourceLocator()).contains("#L");
            assertThat(record.definition()).isNull();
            assertThat(record.root() != null || record.pluralForm() != null).isTrue();
            assertThat(validator.validate(record, 1).valid()).isTrue();
        });
        assertThat(records.stream().map(record -> record.root()).filter(root -> root != null).collect(Collectors.toSet())).hasSize(417);
        assertThat(records.stream()
                .map(record -> validator.validate(record, 1).record().normalizedLemma())
                .collect(Collectors.toSet()))
                .contains(
                        "\u0643\u062A\u0627\u0628",
                        "\u0643\u0627\u062A\u0628",
                        "\u0645\u0643\u062A\u0648\u0628",
                        "\u0645\u0643\u062A\u0628\u0629",
                        "\u0643\u062A\u0627\u0628\u0629",
                        "\u0634\u0645\u0633",
                        "\u0642\u0645\u0631",
                        "\u0628\u0627\u0628",
                        "\u0639\u064A\u0646",
                        "\u0645\u062F\u0631\u0633\u0629");

        var manifest = JsonMapper.builder().build().readTree(Files.readString(MANIFEST));
        assertThat(manifest.path("source").path("license").asString()).isEqualTo("CC BY-SA 3.0");
        assertThat(manifest.path("source").path("licenseDecision").asString()).isEqualTo("APPROVED");
        assertThat(manifest.path("source").path("commercialReuse").asBoolean()).isTrue();
        assertThat(manifest.path("source").path("attribution").asString()).contains("Polit\u00e8cnica de Val\u00e8ncia");
    }

    @Test
    void normalizationPreservesDisplayTextWhileFoldingDiacriticsAndAlefForms() {
        var normalization = new ArabicTextNormalizationService();
        String bare = "\u0643\u062A\u0627\u0628";
        String vocalized = "\u0643\u0650\u062A\u0627\u0628";
        String article = "\u0627\u0644\u0643\u062A\u0627\u0628";
        String alefForms = "\u0623\u0625\u0622\u0627";

        assertThat(normalization.normalize(vocalized).originalText()).isEqualTo(vocalized);
        assertThat(normalization.normalize(vocalized).normalizedText()).isEqualTo(normalization.normalize(bare).normalizedText());
        assertThat(normalization.normalize(article).normalizedText()).isEqualTo(article);
        assertThat(normalization.normalize("\u0627\u0627\u0627\u0627").normalizedText())
                .isEqualTo(normalization.normalize(alefForms).normalizedText());
        assertThat(normalization.normalize("\u0643\u0640\u062A\u0627\u0628").normalizedText())
                .isEqualTo(normalization.normalize(bare).normalizedText());
    }
}
