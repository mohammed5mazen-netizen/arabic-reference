package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.mrsoft.arabicreference.contentimport.application.ContentImportException;
import com.mrsoft.arabicreference.contentimport.application.ContentImportService;
import com.mrsoft.arabicreference.contentimport.cli.ContentImportRunner;
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
}
