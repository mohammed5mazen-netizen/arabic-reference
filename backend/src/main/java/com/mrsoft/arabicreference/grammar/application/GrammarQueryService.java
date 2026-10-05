package com.mrsoft.arabicreference.grammar.application;

import com.mrsoft.arabicreference.grammar.application.GrammarViews.CategoryCard;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.LexicalLink;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.Link;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.MorphologyLink;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PageResult;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicAnnotation;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicComponent;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicConcept;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicExample;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicIndex;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicRelation;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicRule;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicToken;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.PublicTopic;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.SearchHit;
import com.mrsoft.arabicreference.grammar.application.GrammarViews.SourceLine;
import com.mrsoft.arabicreference.grammar.domain.ArabicPhrase;
import com.mrsoft.arabicreference.grammar.domain.GrammarCategory;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarAnnotationEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarAnnotationRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarConceptRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarRuleRepository;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTopicEntity;
import com.mrsoft.arabicreference.grammar.infrastructure.persistence.GrammarTopicRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrammarQueryService {

    private static final String INTRODUCTION = "مرجع منظّم في النحو العربي. الموضوعات والقواعد والمصطلحات المنشورة هنا تمر بمراجعة تحريرية، وتُعرض من لقطة منشورة لا من المسودة.";

    private final GrammarTopicRepository topics;
    private final GrammarRuleRepository rules;
    private final GrammarConceptRepository concepts;
    private final GrammarAnnotationRepository annotations;
    private final JdbcTemplate jdbc;
    private final ArabicTextNormalizer normalizer = new ArabicTextNormalizer();

    public GrammarQueryService(
            GrammarTopicRepository topics,
            GrammarRuleRepository rules,
            GrammarConceptRepository concepts,
            GrammarAnnotationRepository annotations,
            JdbcTemplate jdbc) {
        this.topics = topics;
        this.rules = rules;
        this.concepts = concepts;
        this.annotations = annotations;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public PublicIndex index(int page, int size) {
        int bounded = bound(page, size);
        Page<GrammarTopicEntity> result = topics.published(PublicationStatus.ARCHIVED, PageRequest.of(page, bounded));
        Map<GrammarCategory, Long> counts = new EnumMap<>(GrammarCategory.class);
        jdbc.query("""
                select published_category, count(*) as total
                from grammar_topic
                where published_snapshot is not null and status <> 'ARCHIVED' and published_category is not null
                group by published_category
                """, row -> {
            counts.put(GrammarCategory.valueOf(row.getString("published_category")), row.getLong("total"));
        });
        List<CategoryCard> categories = new ArrayList<>();
        for (GrammarCategory category : GrammarCategory.values()) {
            categories.add(new CategoryCard(category.name(), category.arabicLabel(), counts.getOrDefault(category, 0L)));
        }
        return new PublicIndex("النحو", INTRODUCTION, categories, new PageResult<>(result.map(this::topicLink).toList(), page, bounded, result.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public PublicTopic topic(String slug) {
        GrammarTopicEntity topic = visibleTopic(slug);
        Map<String, Object> snapshot = topic.getPublishedSnapshot();
        List<Link> ancestors = new ArrayList<>();
        UUID parentId = topic.getPublishedParentId();
        int guard = 0;
        while (parentId != null && guard++ < 32) {
            GrammarTopicEntity parent = topics.findById(parentId).orElse(null);
            if (parent == null || !visible(parent)) {
                break;
            }
            ancestors.add(0, topicLink(parent));
            parentId = parent.getPublishedParentId();
        }
        List<Link> children = topics.publishedChildren(topic.getId(), PublicationStatus.ARCHIVED).stream().map(this::topicLink).toList();
        List<Link> ruleLinks = rules.publishedForTopic(topic.getId(), PublicationStatus.ARCHIVED).stream().map(this::ruleLink).toList();
        return new PublicTopic(
                text(snapshot, "title"),
                topic.getSlug(),
                text(snapshot, "summary"),
                text(snapshot, "category"),
                text(snapshot, "categoryLabel"),
                text(snapshot, "difficulty"),
                text(snapshot, "difficultyLabel"),
                ancestors,
                children,
                ruleLinks,
                links(snapshot.get("prerequisites")),
                sources(snapshot.get("sources")));
    }

    @Transactional(readOnly = true)
    public PublicRule rule(String slug) {
        GrammarRuleEntity rule = rules.findBySlug(slug).filter(this::visibleRule).orElseThrow(() -> new ResourceNotFoundException("Rule was not found."));
        Map<String, Object> snapshot = rule.getPublishedSnapshot();
        return new PublicRule(
                text(snapshot, "title"),
                rule.getSlug(),
                text(snapshot, "summary"),
                text(snapshot, "ruleText"),
                text(snapshot, "difficulty"),
                text(snapshot, "difficultyLabel"),
                link(snapshot.get("topic")),
                components(snapshot.get("components")),
                examples(snapshot.get("examples")),
                relations(snapshot.get("relations")),
                links(snapshot.get("concepts")),
                sources(snapshot.get("sources")));
    }

    @Transactional(readOnly = true)
    public PublicConcept concept(String slug) {
        GrammarConceptEntity concept = concepts.findBySlug(slug).filter(this::visibleConcept).orElseThrow(() -> new ResourceNotFoundException("Concept was not found."));
        Map<String, Object> snapshot = concept.getPublishedSnapshot();
        return new PublicConcept(
                text(snapshot, "term"),
                concept.getSlug(),
                text(snapshot, "shortDefinition"),
                text(snapshot, "detailedDefinition"),
                strings(snapshot.get("aliases")),
                links(snapshot.get("rules")),
                sources(snapshot.get("sources")));
    }

    @Transactional(readOnly = true)
    public PublicAnnotation annotation(UUID id) {
        GrammarAnnotationEntity annotation = annotations.findById(id).filter(this::visibleAnnotation).orElseThrow(() -> new ResourceNotFoundException("Annotation was not found."));
        Map<String, Object> snapshot = annotation.getPublishedSnapshot();
        List<PublicToken> tokenViews = tokens(snapshot.get("tokens"));
        return new PublicAnnotation(text(snapshot, "sentence"), source(snapshot.get("source")), tokenViews, tokenViews.isEmpty() ? text(snapshot, "annotationNote") : null);
    }

    @Transactional(readOnly = true)
    public PageResult<SearchHit> search(String query, int page, int size) {
        int bounded = bound(page, size);
        if (query == null || query.isBlank() || query.codePointCount(0, query.length()) > 80) {
            throw invalid("q", "Enter an Arabic query of 1 to 80 characters.");
        }
        String normalized = normalizer.normalize(query).normalizedText();
        if (!ArabicPhrase.containsArabic(normalized) || normalized.codePointCount(0, normalized.length()) > 80) {
            throw invalid("q", "Enter an Arabic query of 1 to 80 characters.");
        }
        String like = "%" + normalized.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        String from = """
                from (
                    select distinct kind, slug, title, summary from (
                        select 'TOPIC' as kind, slug, published_title as title, coalesce(published_summary, '') as summary, published_normalized as norm
                        from grammar_topic
                        where published_normalized is not null and status <> 'ARCHIVED'
                        union all
                        select 'RULE', slug, published_title, coalesce(published_summary, ''), published_normalized
                        from grammar_rule
                        where published_normalized is not null and status <> 'ARCHIVED'
                        union all
                        select 'CONCEPT', slug, published_title, coalesce(published_summary, ''), published_normalized
                        from grammar_concept
                        where published_normalized is not null and status <> 'ARCHIVED'
                        union all
                        select 'CONCEPT', c.slug, c.published_title, coalesce(c.published_summary, ''), a.published_normalized
                        from grammar_concept_alias a
                        join grammar_concept c on c.id = a.concept_id
                        where a.published_normalized is not null and c.published_snapshot is not null and c.status <> 'ARCHIVED'
                    ) raw
                    where norm like ? escape '\\'
                ) hits
                """;
        Long total = jdbc.queryForObject("select count(*) " + from, Long.class, like);
        List<SearchHit> items = jdbc.query("select kind, title, slug, summary " + from + " order by title limit ? offset ?", (row, index) -> new SearchHit(row.getString("kind"), row.getString("title"), row.getString("slug"), row.getString("summary")), like, bounded, page * bounded);
        return new PageResult<>(items, page, bounded, total == null ? 0 : total);
    }

    private GrammarTopicEntity visibleTopic(String slug) {
        return topics.findBySlug(slug).filter(this::visible).orElseThrow(() -> new ResourceNotFoundException("Topic was not found."));
    }

    private boolean visible(GrammarTopicEntity topic) {
        return topic.getPublishedSnapshot() != null && topic.getStatus() != PublicationStatus.ARCHIVED;
    }

    private boolean visibleRule(GrammarRuleEntity rule) {
        return rule.getPublishedSnapshot() != null && rule.getStatus() != PublicationStatus.ARCHIVED;
    }

    private boolean visibleConcept(GrammarConceptEntity concept) {
        return concept.getPublishedSnapshot() != null && concept.getStatus() != PublicationStatus.ARCHIVED;
    }

    private boolean visibleAnnotation(GrammarAnnotationEntity annotation) {
        return annotation.getPublishedSnapshot() != null && annotation.getStatus() != PublicationStatus.ARCHIVED;
    }

    private Link topicLink(GrammarTopicEntity topic) {
        Map<String, Object> snapshot = topic.getPublishedSnapshot();
        return new Link(text(snapshot, "title"), topic.getSlug(), text(snapshot, "summary"), text(snapshot, "difficultyLabel"));
    }

    private Link ruleLink(GrammarRuleEntity rule) {
        Map<String, Object> snapshot = rule.getPublishedSnapshot();
        return new Link(text(snapshot, "title"), rule.getSlug(), text(snapshot, "summary"), text(snapshot, "difficultyLabel"));
    }

    private List<PublicComponent> components(Object value) {
        List<PublicComponent> result = new ArrayList<>();
        for (Map<String, Object> map : maps(value)) {
            result.add(new PublicComponent(text(map, "type"), text(map, "typeLabel"), text(map, "heading"), text(map, "body"), sources(map.get("sources"))));
        }
        return result;
    }

    private List<PublicExample> examples(Object value) {
        List<PublicExample> result = new ArrayList<>();
        for (Map<String, Object> map : maps(value)) {
            result.add(new PublicExample(
                    text(map, "textOriginal"),
                    text(map, "explanation"),
                    text(map, "exampleType"),
                    text(map, "exampleTypeLabel"),
                    Boolean.TRUE.equals(map.get("editorial")),
                    text(map, "editorialNote"),
                    integer(map.get("surah")),
                    integer(map.get("ayah")),
                    text(map, "poet"),
                    text(map, "workTitle"),
                    text(map, "verseLocator"),
                    source(map.get("source")),
                    tokens(map.get("tokens")),
                    text(map, "annotationNote")));
        }
        return result;
    }

    private List<PublicToken> tokens(Object value) {
        List<PublicToken> result = new ArrayList<>();
        for (Map<String, Object> map : maps(value)) {
            result.add(new PublicToken(
                    text(map, "surface"),
                    integer(map.get("position")) == null ? 0 : integer(map.get("position")),
                    text(map, "roleLabel"),
                    text(map, "stateLabel"),
                    text(map, "explanation"),
                    lexical(map.get("lexical")),
                    morphology(map.get("morphology"))));
        }
        result.sort((left, right) -> Integer.compare(left.position(), right.position()));
        return result;
    }

    private List<PublicRelation> relations(Object value) {
        List<PublicRelation> result = new ArrayList<>();
        for (Map<String, Object> map : maps(value)) {
            result.add(new PublicRelation(text(map, "type"), text(map, "typeLabel"), text(map, "title"), text(map, "slug")));
        }
        return result;
    }

    private List<Link> links(Object value) {
        List<Link> result = new ArrayList<>();
        for (Map<String, Object> map : maps(value)) {
            result.add(new Link(text(map, "title"), text(map, "slug"), text(map, "summary"), text(map, "difficultyLabel")));
        }
        return result;
    }

    private Link link(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return null;
        }
        Map<String, Object> map = copy(raw);
        return new Link(text(map, "title"), text(map, "slug"), text(map, "summary"), text(map, "difficultyLabel"));
    }

    private List<SourceLine> sources(Object value) {
        List<SourceLine> result = new ArrayList<>();
        for (Map<String, Object> map : maps(value)) {
            SourceLine line = source(map);
            if (line != null) {
                result.add(line);
            }
        }
        return result;
    }

    private SourceLine source(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return null;
        }
        Map<String, Object> map = copy(raw);
        return new SourceLine(text(map, "title"), text(map, "author"), text(map, "edition"), integer(map.get("publicationYear")), integer(map.get("pageFrom")), integer(map.get("pageTo")), text(map, "attributionText"));
    }

    private LexicalLink lexical(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return null;
        }
        Map<String, Object> map = copy(raw);
        return new LexicalLink(text(map, "title"), text(map, "slug"));
    }

    private MorphologyLink morphology(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return null;
        }
        Map<String, Object> map = copy(raw);
        return new MorphologyLink(text(map, "patternOriginal"), text(map, "label"));
    }

    private List<String> strings(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(item.toString());
            }
        }
        return result;
    }

    private List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> raw) {
                result.add(copy(raw));
            }
        }
        return result;
    }

    private static Map<String, Object> copy(Map<?, ?> raw) {
        Map<String, Object> map = new LinkedHashMap<>();
        raw.forEach((key, value) -> map.put(String.valueOf(key), value));
        return map;
    }

    private static String text(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        return value == null ? null : value.toString();
    }

    private static Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private static int bound(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ValidationException("Page request is invalid.", List.of(new FieldErrorDetail("page", "Page size must be from 1 to 50.")));
        }
        return size;
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
