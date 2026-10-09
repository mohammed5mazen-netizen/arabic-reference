package com.mrsoft.arabicreference.contentimport.application;

import com.mrsoft.arabicreference.linguistics.application.ArabicTextNormalizationService;
import org.springframework.stereotype.Component;

@Component
public class NormalizationService {
    private final ArabicTextNormalizationService normalization;

    public NormalizationService(ArabicTextNormalizationService normalization) {
        this.normalization = normalization;
    }

    public String searchForm(String original) {
        return normalization.normalize(original).normalizedText();
    }
}
