package com.mrsoft.arabicreference.linguistics.application;

import com.mrsoft.arabicreference.linguistics.domain.text.ArabicText;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import org.springframework.stereotype.Service;

@Service
public class ArabicTextNormalizationService {

    private final ArabicTextNormalizer normalizer = new ArabicTextNormalizer();

    public ArabicText normalize(String originalText) {
        return normalizer.normalize(originalText);
    }
}
