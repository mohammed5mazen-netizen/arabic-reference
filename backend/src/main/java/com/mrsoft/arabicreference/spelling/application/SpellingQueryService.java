package com.mrsoft.arabicreference.spelling.application;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.SpellingMatch;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.PublicLink;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.PublicRule;
import com.mrsoft.arabicreference.spelling.application.SpellingViews.PublicTopic;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingRuleEntity;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingRuleRepository;
import com.mrsoft.arabicreference.spelling.infrastructure.persistence.SpellingTopicRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpellingQueryService {

    private final SpellingTopicRepository topics;
    private final SpellingRuleRepository rules;
    private final ArabicTextNormalizer normalizer = new ArabicTextNormalizer();

    public SpellingQueryService(SpellingTopicRepository topics, SpellingRuleRepository rules) {
        this.topics = topics;
        this.rules = rules;
    }

    @Transactional(readOnly = true)
    public List<PublicLink> topics() {
        List<PublicLink> links = new ArrayList<>();
        for (var topic : topics.visibleToPublic(PublicationStatus.ARCHIVED)) {
            Map<String, Object> snapshot = topic.getPublishedSnapshot();
            links.add(new PublicLink(text(snapshot.get("title")), text(snapshot.get("slug")), text(snapshot.get("summary"))));
        }
        return links;
    }

    @Transactional(readOnly = true)
    public PublicTopic topic(String slug) {
        var topic = topics.findBySlug(slug).filter(item -> item.visibleToPublic()).orElseThrow(() -> missing("Topic"));
        Map<String, Object> snapshot = topic.getPublishedSnapshot();
        List<PublicLink> ruleLinks = new ArrayList<>();
        for (SpellingRuleEntity rule : rules.visibleToPublic(PublicationStatus.ARCHIVED)) {
            Map<String, Object> ruleSnapshot = rule.getPublishedSnapshot();
            if (topic.getId().toString().equals(text(ruleSnapshot.get("topicId")))) {
                ruleLinks.add(new PublicLink(text(ruleSnapshot.get("title")), text(ruleSnapshot.get("slug")), text(ruleSnapshot.get("summary"))));
            }
        }
        return new PublicTopic(text(snapshot.get("title")), text(snapshot.get("slug")), text(snapshot.get("summary")), ruleLinks, maps(snapshot.get("sources")));
    }

    @Transactional(readOnly = true)
    public PublicRule rule(String slug) {
        var rule = rules.findBySlug(slug).filter(item -> item.visibleToPublic()).orElseThrow(() -> missing("Rule"));
        Map<String, Object> snapshot = rule.getPublishedSnapshot();
        Map<String, Object> topic = map(snapshot.get("topic"));
        return new PublicRule(
                text(snapshot.get("title")),
                text(snapshot.get("slug")),
                text(snapshot.get("summary")),
                text(snapshot.get("coreRule")),
                text(snapshot.get("difficulty")),
                text(snapshot.get("difficultyLabel")),
                new PublicLink(text(topic.get("title")), text(topic.get("slug")), text(topic.get("summary"))),
                maps(snapshot.get("clauses")),
                maps(snapshot.get("examples")),
                maps(snapshot.get("sources")));
    }

    @Transactional(readOnly = true)
    public List<SpellingMatch> matches(String normalized) {
        List<SpellingMatch> matches = new ArrayList<>();
        int scanned = 0;
        for (SpellingRuleEntity rule : rules.visibleToPublic(PublicationStatus.ARCHIVED)) {
            if (matches.size() >= 12 || scanned >= 200) {
                break;
            }
            scanned++;
            Map<String, Object> snapshot = rule.getPublishedSnapshot();
            for (Map<String, Object> example : maps(snapshot.get("examples"))) {
                if (matches.size() >= 12) {
                    break;
                }
                if (same(normalized, text(example.get("correctForm")))
                        || same(normalized, text(example.get("incorrectForm")))
                        || same(normalized, text(example.get("commonForm")))) {
                    matches.add(new SpellingMatch(
                            text(snapshot.get("title")),
                            text(snapshot.get("slug")),
                            text(example.get("kind")),
                            blank(example.get("correctForm")),
                            blank(example.get("incorrectForm")),
                            blank(example.get("commonForm")),
                            blank(example.get("explanation")),
                            blank(example.get("contextNote")),
                            blank(example.get("reason"))));
                }
            }
        }
        return matches;
    }

    private boolean same(String normalized, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return normalized.equals(normalizer.normalize(value).normalizedText());
    }

    private static String blank(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> raw ? (Map<String, Object>) raw : Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> items)) {
            return List.of();
        }
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> raw) {
                maps.add((Map<String, Object>) raw);
            }
        }
        return maps;
    }

    private static ResourceNotFoundException missing(String name) {
        return new ResourceNotFoundException(name + " was not found.");
    }
}
