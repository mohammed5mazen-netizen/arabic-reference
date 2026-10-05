package com.mrsoft.arabicreference.spelling.application;

import com.mrsoft.arabicreference.content.domain.KnowledgeTargetSource;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingRuleRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SpellingKnowledgeTarget implements KnowledgeTargetSource {

    private final SpellingRuleRepository rules;

    public SpellingKnowledgeTarget(SpellingRuleRepository rules) {
        this.rules = rules;
    }

    @Override
    public boolean supports(KnowledgeTargetType type) {
        return type == KnowledgeTargetType.SPELLING_RULE;
    }

    @Override
    public Optional<KnowledgeTarget> published(UUID id) {
        return rules.findById(id).filter(rule -> rule.visibleToPublic()).map(rule -> {
            Map<String, Object> snapshot = rule.getPublishedSnapshot();
            String title = snapshot.get("title") == null ? "" : String.valueOf(snapshot.get("title"));
            String slug = snapshot.get("slug") == null ? "" : String.valueOf(snapshot.get("slug"));
            return new KnowledgeTarget(KnowledgeTargetType.SPELLING_RULE, id, title, "/spelling/rules/" + slug);
        });
    }
}
