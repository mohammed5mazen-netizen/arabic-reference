package com.mrsoft.arabicreference.ai.application;

import com.mrsoft.arabicreference.ai.application.AiModelPort.ModelCompletion;
import com.mrsoft.arabicreference.ai.application.AiModelPort.ModelRequest;
import com.mrsoft.arabicreference.ai.application.AiUsagePort.UsageRecord;
import com.mrsoft.arabicreference.ai.application.AiViews.AssistantAnswer;
import com.mrsoft.arabicreference.ai.application.AiViews.NearbyLink;
import com.mrsoft.arabicreference.ai.application.AiViews.ToolHint;
import com.mrsoft.arabicreference.ai.application.CitationGuard.Guarded;
import com.mrsoft.arabicreference.ai.application.EvidenceSelector.Selected;
import com.mrsoft.arabicreference.ai.application.KnowledgeRetrievalPort.KnowledgeStamp;
import com.mrsoft.arabicreference.ai.application.KnowledgeRetrievalPort.RetrievalBatch;
import com.mrsoft.arabicreference.ai.application.KnowledgeRetrievalPort.RetrievalRequest;
import com.mrsoft.arabicreference.ai.application.QuestionClassifier.Resolution;
import com.mrsoft.arabicreference.ai.domain.GroundingStatus;
import com.mrsoft.arabicreference.ai.domain.RetrievedEvidence;
import com.mrsoft.arabicreference.shared.kernel.exception.ServiceUnavailableException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    private final AiPropertiesView properties;
    private final AiModelPort model;
    private final KnowledgeRetrievalPort retrieval;
    private final AiRateLimitPort rateLimit;
    private final AiAnswerCachePort cache;
    private final AiUsagePort usage;
    private final AiMetrics metrics;

    public AssistantService(
            AiPropertiesView properties,
            AiModelPort model,
            KnowledgeRetrievalPort retrieval,
            AiRateLimitPort rateLimit,
            AiAnswerCachePort cache,
            AiUsagePort usage,
            AiMetrics metrics) {
        this.properties = properties;
        this.model = model;
        this.retrieval = retrieval;
        this.rateLimit = rateLimit;
        this.cache = cache;
        this.usage = usage;
        this.metrics = metrics;
    }

    public AiViews.AssistantStatus status() {
        List<String> suggestions = properties.operational() ? retrieval.suggestions() : List.of();
        return new AiViews.AssistantStatus(properties.operational(), properties.operational() ? "إجابات موثقة من محتوى المرجع" : AiTexts.DISABLED, suggestions);
    }

    public AiViews.AdminAiStatus adminStatus() {
        var snapshot = usage.snapshot();
        return new AiViews.AdminAiStatus(
                properties.operational(),
                properties.provider(),
                properties.model(),
                properties.keyConfigured(),
                com.mrsoft.arabicreference.ai.domain.PromptVersion.CURRENT,
                snapshot.requests(),
                snapshot.grounded(),
                snapshot.partial(),
                snapshot.insufficient(),
                snapshot.providerErrors(),
                snapshot.averageLatencyMs());
    }

    public AssistantAnswer ask(String question, List<AiViews.PriorTurn> prior, String client) {
        long started = System.nanoTime();
        var sample = metrics.start();
        if (!properties.operational()) {
            metrics.record("DISABLED", 0, sample);
            throw new ServiceUnavailableException(AiTexts.DISABLED);
        }
        AiTexts.Prepared prepared = AiTexts.prepare(question, properties.maxQuestionCodePoints(), properties.maxQuestionLines());
        rateLimit.acquire(client);
        Resolution resolution = QuestionClassifier.classify(prepared.normalized());
        KnowledgeStamp stamp = retrieval.stamp();
        String cacheKey = AiCacheKey.of(prepared.normalized(), stamp.contentStamp(), stamp.ruleGeneration(), stamp.searchGeneration(), stamp.indexVersion(), properties.model());
        if (properties.cacheEnabled()) {
            AssistantAnswer cached = cache.read(cacheKey);
            if (cached != null) {
                metrics.record(cached.grounding(), cached.evidence().size(), sample);
                return cached.withRequestId(UUID.randomUUID().toString());
            }
        }
        String requestId = UUID.randomUUID().toString();
        RetrievalBatch batch = retrieval.retrieve(new RetrievalRequest(resolution.intent(), prepared.display(), prepared.normalized(), resolution.terms(), resolution.asksForParsing()), client);
        EvidenceSelector.Selection selection = EvidenceSelector.select(batch.evidence(), properties.maxContextItems(), properties.maxExcerptChars(), properties.totalBudget());
        List<ToolHint> tools = toolHints(resolution, selection);
        List<NearbyLink> nearby = nearby(selection);
        if (!selection.hasUsable()) {
            List<String> limitations = new ArrayList<>();
            limitations.add(AiTexts.INSUFFICIENT);
            if (resolution.asksForParsing()) {
                limitations.add(AiTexts.PARSING_LIMIT);
            }
            AssistantAnswer answer = AiViews.insufficient(requestId, AiTexts.INSUFFICIENT, limitations, nearby, tools);
            remember(cacheKey, answer, "INSUFFICIENT_EVIDENCE", 0, null, null, sample, started);
            return answer;
        }
        ModelRequest request = ContextBuilder.build(selection.usable(), prepared.display(), prior, properties.maxOutputTokens());
        ModelCompletion completion;
        try {
            completion = model.complete(request);
        } catch (AiProviderException exception) {
            usage.record(new UsageRecord(properties.provider(), properties.model(), "PROVIDER_ERROR", elapsed(started), null, null, selection.usable().size()));
            metrics.record("PROVIDER_ERROR", selection.usable().size(), sample);
            throw new ServiceUnavailableException(exception.kind() == AiProviderException.Kind.TIMEOUT ? AiTexts.TIMEOUT : AiTexts.PROVIDER_FAILURE);
        }
        boolean missingSide = resolution.intent() == com.mrsoft.arabicreference.ai.domain.AssistantIntent.COMPARISON && resolution.terms().size() < 2;
        boolean comparisonGap = resolution.intent() == com.mrsoft.arabicreference.ai.domain.AssistantIntent.COMPARISON && !batch.semanticRelation();
        Guarded guarded = CitationGuard.accept(completion, selection.usable(), resolution.asksForParsing(), comparisonGap, missingSide);
        if (guarded.status() == GroundingStatus.INSUFFICIENT_EVIDENCE) {
            AssistantAnswer answer = AiViews.insufficient(requestId, guarded.answer(), guarded.limitations(), nearby, tools);
            remember(cacheKey, answer, guarded.status().name(), 0, completion.inputTokens(), completion.outputTokens(), sample, started);
            return answer;
        }
        AssistantAnswer answer = new AssistantAnswer(
                requestId,
                guarded.answer(),
                guarded.status().name(),
                guarded.status().label(),
                true,
                guarded.uncertain(),
                guarded.citations(),
                guarded.evidence(),
                guarded.limitations(),
                nearby,
                tools);
        remember(cacheKey, answer, guarded.status().name(), guarded.evidence().size(), completion.inputTokens(), completion.outputTokens(), sample, started);
        return answer;
    }

    private void remember(String cacheKey, AssistantAnswer answer, String status, int evidenceCount, Integer inputTokens, Integer outputTokens, io.micrometer.core.instrument.Timer.Sample sample, long started) {
        if (properties.cacheEnabled()) {
            cache.write(cacheKey, answer);
        }
        usage.record(new UsageRecord(properties.provider(), properties.model(), status, elapsed(started), inputTokens, outputTokens, evidenceCount));
        metrics.record(status, evidenceCount, sample);
    }

    private static int elapsed(long started) {
        long millis = (System.nanoTime() - started) / 1_000_000L;
        return millis > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) millis;
    }

    private static List<NearbyLink> nearby(EvidenceSelector.Selection selection) {
        List<NearbyLink> links = new ArrayList<>();
        for (RetrievedEvidence item : selection.nearby()) {
            if (item.canonicalUrl() == null || item.canonicalUrl().isBlank()) {
                continue;
            }
            links.add(new NearbyLink(item.title(), item.canonicalUrl(), "نتيجة قريبة"));
        }
        return links;
    }

    private static List<ToolHint> toolHints(Resolution resolution, EvidenceSelector.Selection selection) {
        String term = resolution.terms().isEmpty() ? "" : resolution.terms().get(0);
        String query = term.isBlank() ? "" : "?q=" + term;
        List<ToolHint> hints = new ArrayList<>();
        hints.add(switch (resolution.intent()) {
            case WORD_MEANING -> new ToolHint("محلل الكلمة", "/tools/word-analysis" + query);
            case ROOT -> new ToolHint("مستكشف الجذر", "/tools/root" + query);
            case MORPHOLOGY -> new ToolHint("المحلل الصرفي", "/tools/morphology" + (term.isBlank() ? "" : "?word=" + term));
            case GRAMMAR -> new ToolHint("مستكشف القواعد", "/tools/grammar" + query);
            case SPELLING -> new ToolHint("التحقق الإملائي المرجعي", "/tools/spelling-check" + query);
            case COMPARISON -> new ToolHint("مقارنة الكلمات", "/tools/compare");
            case RHETORIC -> new ToolHint("البلاغة", "/rhetoric");
            case LITERATURE -> new ToolHint("الأدب", "/literature");
            case GENERAL_LINGUISTIC -> new ToolHint("الأدوات اللغوية", "/tools");
        });
        hints.add(new ToolHint("البحث في المرجع", "/search" + query));
        if (!selection.usable().isEmpty()) {
            Selected first = selection.usable().get(0);
            if (first.evidence().canonicalUrl() != null) {
                hints.add(new ToolHint("اقرأ في المرجع", first.evidence().canonicalUrl()));
            }
        }
        return hints;
    }
}
