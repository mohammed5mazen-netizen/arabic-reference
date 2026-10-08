package com.mrsoft.arabicreference.ai;

import com.mrsoft.arabicreference.ai.application.AiModelPort;
import com.mrsoft.arabicreference.ai.application.AiProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScriptedAiModel implements AiModelPort {

    public static final ScriptedAiModel INSTANCE = new ScriptedAiModel();
    public static final AtomicInteger CALLS = new AtomicInteger();
    public static final AtomicReference<ModelRequest> LAST = new AtomicReference<>();
    public static final AtomicReference<java.util.function.Function<ModelRequest, ModelCompletion>> NEXT = new AtomicReference<>();

    private ScriptedAiModel() {
    }

    public static void reset() {
        CALLS.set(0);
        LAST.set(null);
        NEXT.set(null);
    }

    @Override
    public ModelCompletion complete(ModelRequest request) {
        CALLS.incrementAndGet();
        LAST.set(request);
        var script = NEXT.getAndSet(null);
        if (script != null) {
            return script.apply(request);
        }
        List<String> ids = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\[E\\d+]").matcher(request.evidenceBlock());
        while (matcher.find() && ids.size() < 4) {
            String token = matcher.group();
            ids.add(token.substring(1, token.length() - 1));
        }
        return new ModelCompletion("هذه إجابة مبنية على الأدلة المسترجعة.", ids.isEmpty() ? List.of("E1") : ids, List.of(), 12, 18);
    }

    public static ModelCompletion fail(AiProviderException.Kind kind) {
        throw new AiProviderException(kind);
    }
}
