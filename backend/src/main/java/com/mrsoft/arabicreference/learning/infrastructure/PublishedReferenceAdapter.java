package com.mrsoft.arabicreference.learning.infrastructure;

import com.mrsoft.arabicreference.content.application.ArticleQueryService;
import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery;
import com.mrsoft.arabicreference.grammar.application.GrammarQueryService;
import com.mrsoft.arabicreference.learning.application.PublishedReferencePort;
import com.mrsoft.arabicreference.learning.domain.ReferenceKind;
import com.mrsoft.arabicreference.rhetoric.application.RhetoricQueryService;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.spelling.application.SpellingQueryService;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PublishedReferenceAdapter implements PublishedReferencePort {

    private final PublishedDictionaryQuery dictionary;
    private final GrammarQueryService grammar;
    private final SpellingQueryService spelling;
    private final RhetoricQueryService rhetoric;
    private final ArticleQueryService articles;

    public PublishedReferenceAdapter(
            PublishedDictionaryQuery dictionary,
            GrammarQueryService grammar,
            SpellingQueryService spelling,
            RhetoricQueryService rhetoric,
            ArticleQueryService articles) {
        this.dictionary = dictionary;
        this.grammar = grammar;
        this.spelling = spelling;
        this.rhetoric = rhetoric;
        this.articles = articles;
    }

    @Override
    public Optional<ResolvedReference> resolve(ReferenceKind kind, String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        try {
            return switch (kind) {
                case DICTIONARY_ENTRY -> dictionary.lemmaBySlug(slug).map(lemma -> new ResolvedReference(lemma.lemmaOriginal(), "/word/" + lemma.slug()));
                case GRAMMAR_RULE -> Optional.of(new ResolvedReference(grammar.rule(slug).title(), "/grammar/rules/" + slug));
                case GRAMMAR_CONCEPT -> Optional.of(new ResolvedReference(grammar.concept(slug).term(), "/grammar/concepts/" + slug));
                case GRAMMAR_TOPIC -> Optional.of(new ResolvedReference(grammar.topic(slug).title(), "/grammar/" + slug));
                case SPELLING_RULE -> Optional.of(new ResolvedReference(spelling.rule(slug).title(), "/spelling/rules/" + slug));
                case RHETORIC_DEVICE -> Optional.of(new ResolvedReference(rhetoric.device(slug).name(), "/rhetoric/devices/" + slug));
                case ARTICLE -> Optional.of(new ResolvedReference(articles.article(slug).title(), "/articles/" + slug));
                case MORPHOLOGY_TOOL -> "analyze".equals(slug)
                        ? Optional.of(new ResolvedReference("تحليل الكلمة", "/tools/morphology"))
                        : Optional.empty();
            };
        } catch (ResourceNotFoundException missing) {
            return Optional.empty();
        }
    }
}
