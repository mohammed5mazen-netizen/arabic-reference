package com.mrsoft.arabicreference.morphology.api;

import com.mrsoft.arabicreference.morphology.application.MorphologyService;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.ConjugationView;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.EntryMorphology;
import com.mrsoft.arabicreference.morphology.application.MorphologyViews.RootMorphology;
import com.mrsoft.arabicreference.morphology.domain.AnalysisReport;
import com.mrsoft.arabicreference.shared.web.ClientAddresses;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicMorphologyController {

    private final MorphologyService morphology;
    private final TimeProvider timeProvider;

    public PublicMorphologyController(MorphologyService morphology, TimeProvider timeProvider) {
        this.morphology = morphology;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/api/v1/public/morphology/analyze")
    public ApiResponse<AnalysisReport> analyze(@RequestParam(name = "word", required = false) String word, HttpServletRequest request) {
        return ApiResponses.ok(morphology.analyze(word, ClientAddresses.read(request)), timeProvider);
    }

    @GetMapping("/api/v1/public/morphology/conjugate")
    public ApiResponse<ConjugationView> conjugate(@RequestParam UUID entryId) {
        return ApiResponses.ok(morphology.conjugate(entryId), timeProvider);
    }

    @GetMapping("/api/v1/public/dictionary/entries/{id}/morphology")
    public ApiResponse<EntryMorphology> entry(@PathVariable UUID id) {
        return ApiResponses.ok(morphology.entryMorphology(id), timeProvider);
    }

    @GetMapping("/api/v1/public/morphology/roots/{slug}")
    public ApiResponse<RootMorphology> root(@PathVariable String slug) {
        return ApiResponses.ok(morphology.rootMorphology(slug), timeProvider);
    }

}
