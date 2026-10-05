package com.mrsoft.arabicreference.tools.application;

/**
 * Shared cache key for every linguistic tool. A publication or rule change changes the key.
 */
public final class ToolCacheKey {

    private ToolCacheKey() {
    }

    public static String of(String tool, String normalized, String dictionaryStamp, long morphologyGeneration, long searchGeneration, int indexVersion) {
        return tool + "\n" + normalized + "\n" + dictionaryStamp + "\n" + morphologyGeneration + "\n" + searchGeneration + "\n" + indexVersion;
    }
}
