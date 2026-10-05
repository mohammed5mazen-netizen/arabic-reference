package com.mrsoft.arabicreference.grammar.application;

import com.mrsoft.arabicreference.content.domain.KnowledgeTargetSource;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GrammarKnowledgeTarget implements KnowledgeTargetSource {

    private final GrammarRuleRepository rules;

    public GrammarKnowledgeTarget(GrammarRuleRepository rules) {
        this.rules = rules;
    }

    @Override
    public boolean supports(KnowledgeTargetType type) {
        return type == KnowledgeTargetType.GRAMMAR_RULE;
    }

    @Override
    public Optional<KnowledgeTarget> published(UUID id) {
        return rules.findById(id)
                .filter(rule -> rule.getPublishedSnapshot() != null && rule.getStatus() != PublicationStatus.ARCHIVED)
                .map(rule -> {
                    Map<String, Object> snapshot = rule.getPublishedSnapshot();
                    String title = snapshot.get("title") == null ? "" : String.valueOf(snapshot.get("title"));
                    String slug = snapshot.get("slug") == null ? "" : String.valueOf(snapshot.get("slug"));
                    return new KnowledgeTarget(KnowledgeTargetType.GRAMMAR_RULE, id, title, "/grammar/rules/" + slug);
                });
    }
}
