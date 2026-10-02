package com.mrsoft.arabicreference.dictionary.api;

import com.mrsoft.arabicreference.dictionary.application.DictionaryEntryService;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.LookupHit;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PageResult;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicEntry;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicRoot;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/dictionary")
public class PublicDictionaryController {

    private final DictionaryEntryService entries;
    private final TimeProvider timeProvider;

    public PublicDictionaryController(DictionaryEntryService entries, TimeProvider timeProvider) {
        this.entries = entries;
        this.timeProvider = timeProvider;
    }

    @GetMapping("/entries/{id}")
    public ApiResponse<PublicEntry> entry(@PathVariable UUID id) {
        return ApiResponses.ok(entries.publishedById(id), timeProvider);
    }

    @GetMapping("/by-slug/{slug}")
    public ApiResponse<PublicEntry> bySlug(@PathVariable String slug) {
        return ApiResponses.ok(entries.publishedBySlug(slug), timeProvider);
    }

    @GetMapping("/lookup")
    public ApiResponse<PageResult<LookupHit>> lookup(
            @RequestParam(name = "word", required = false) String word,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(entries.lookup(word, page, size), timeProvider);
    }

    @GetMapping("/roots/{root}")
    public ApiResponse<PublicRoot> root(@PathVariable String root) {
        return ApiResponses.ok(entries.publishedRoot(root), timeProvider);
    }
}
