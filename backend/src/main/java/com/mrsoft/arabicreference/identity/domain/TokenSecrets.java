package com.mrsoft.arabicreference.identity.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class TokenSecrets {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private TokenSecrets() {
    }

    public static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            char[] hex = new char[digest.length * 2];
            for (int index = 0; index < digest.length; index++) {
                int value = digest[index] & 0xff;
                hex[index * 2] = HEX[value >>> 4];
                hex[index * 2 + 1] = HEX[value & 0x0f];
            }
            return new String(hex);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required.", exception);
        }
    }
}
