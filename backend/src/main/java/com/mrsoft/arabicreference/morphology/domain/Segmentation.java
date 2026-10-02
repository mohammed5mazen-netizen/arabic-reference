package com.mrsoft.arabicreference.morphology.domain;

import java.util.List;

public record Segmentation(List<String> clitics, List<String> prefixes, String stem, List<String> suffixes) {

    public Segmentation {
        clitics = clitics == null ? List.of() : List.copyOf(clitics);
        prefixes = prefixes == null ? List.of() : List.copyOf(prefixes);
        suffixes = suffixes == null ? List.of() : List.copyOf(suffixes);
    }

    public static Segmentation identity(String stem) {
        return new Segmentation(List.of(), List.of(), stem, List.of());
    }
}
