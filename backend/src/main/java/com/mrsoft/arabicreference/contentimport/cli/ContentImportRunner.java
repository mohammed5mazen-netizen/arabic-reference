package com.mrsoft.arabicreference.contentimport.cli;

import com.mrsoft.arabicreference.contentimport.application.ContentImportException;
import com.mrsoft.arabicreference.contentimport.application.ContentImportService;
import com.mrsoft.arabicreference.contentimport.domain.ImportReport;
import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
@ConditionalOnProperty(name = "content.import.enabled", havingValue = "true")
public class ContentImportRunner implements ApplicationRunner {
    private final ContentImportService imports;
    private final JsonMapper mapper;

    public ContentImportRunner(ContentImportService imports, JsonMapper mapper) {
        this.imports = imports;
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        String file = requiredOption(arguments, "file");
        String source = requiredOption(arguments, "source");
        boolean dryRun = booleanOption(arguments, "dry-run");
        ImportReport report = imports.importPack(Path.of(file), source, dryRun);
        try {
            System.out.println(mapper.writeValueAsString(report));
        } catch (JacksonException exception) {
            throw new ContentImportException("Could not print content import report.", exception);
        }
        if (!dryRun && report.invalid() > 0) {
            throw new ContentImportException("Import was not written because the content pack has invalid records.");
        }
    }

    private static String requiredOption(ApplicationArguments arguments, String name) {
        List<String> values = arguments.getOptionValues(name);
        if (values == null || values.size() != 1 || values.getFirst().isBlank()) {
            throw new ContentImportException("Exactly one --" + name + " option is required.");
        }
        return values.getFirst();
    }

    private static boolean booleanOption(ApplicationArguments arguments, String name) {
        if (!arguments.containsOption(name)) return false;
        List<String> values = arguments.getOptionValues(name);
        if (values == null || values.isEmpty()) return true;
        if (values.size() != 1 || (!"true".equalsIgnoreCase(values.getFirst()) && !"false".equalsIgnoreCase(values.getFirst()))) {
            throw new ContentImportException("--" + name + " must be true or false.");
        }
        return Boolean.parseBoolean(values.getFirst());
    }
}
