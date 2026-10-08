package com.mrsoft.arabicreference.ai.application;

import com.mrsoft.arabicreference.ai.domain.AssistantIntent;
import com.mrsoft.arabicreference.ai.domain.PromptVersion;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class AiCacheKey {

    private AiCacheKey() {
    }

    public static String of(String normalized, String contentStamp, long ruleGeneration, long searchGeneration, int indexVersion, String model) {
        String material = normalized + "\n" + contentStamp + "\n" + ruleGeneration + "\n" + searchGeneration + "\n" + indexVersion + "\n" + PromptVersion.CURRENT + "\n" + model;
        return "ai:" + sha256(material);
    }

    private static String sha256(String material) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
