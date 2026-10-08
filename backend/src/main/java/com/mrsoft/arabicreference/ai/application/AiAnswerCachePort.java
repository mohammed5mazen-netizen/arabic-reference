package com.mrsoft.arabicreference.ai.application;

import com.mrsoft.arabicreference.ai.application.AiViews.AssistantAnswer;

public interface AiAnswerCachePort {

    AssistantAnswer read(String key);

    void write(String key, AssistantAnswer answer);
}
