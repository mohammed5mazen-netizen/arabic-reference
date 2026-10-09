package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mrsoft.arabicreference.contentimport.application.ContentImportException;
import com.mrsoft.arabicreference.contentimport.application.ContentImportService;
import com.mrsoft.arabicreference.contentimport.domain.ImportReport;
import com.mrsoft.arabicreference.contentimport.cli.ContentImportRunner;
import java.nio.file.Path;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

class ContentImportRunnerTest {
    @Test
    void refusesToImportWhenTheApplicationHasAWebContext() {
        ContentImportService imports = mock(ContentImportService.class);
        var runner = new ContentImportRunner(imports, JsonMapper.builder().build(), mock(WebApplicationContext.class));

        assertThatThrownBy(() -> runner.run(mock(ApplicationArguments.class)))
                .isInstanceOf(ContentImportException.class)
                .hasMessageContaining("CLI-only");
        verifyNoInteractions(imports);
    }

    @Test
    void defaultsToDryRunUnlessExplicitlyDisabled() {
        ContentImportService imports = mock(ContentImportService.class);
        ApplicationArguments arguments = mock(ApplicationArguments.class);
        when(arguments.getOptionValues("file")).thenReturn(List.of("pack.ndjson"));
        when(arguments.getOptionValues("source")).thenReturn(List.of("licensed-source"));
        when(arguments.containsOption("dry-run")).thenReturn(false);
        when(imports.importPack(Path.of("pack.ndjson"), "licensed-source", true))
                .thenReturn(new ImportReport("licensed-source", true, false, 1, 1, 0, 0, 1, 0, List.of(), List.of()));

        new ContentImportRunner(imports, JsonMapper.builder().build(), mock(ApplicationContext.class)).run(arguments);

        verify(imports).importPack(Path.of("pack.ndjson"), "licensed-source", true);
    }
}
