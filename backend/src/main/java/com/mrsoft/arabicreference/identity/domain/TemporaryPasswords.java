package com.mrsoft.arabicreference.identity.domain;

import java.security.SecureRandom;

public final class TemporaryPasswords {

    private static final char[] UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final char[] LOWER = "abcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final char[] DIGITS = "23456789".toCharArray();
    private static final char[] SPECIAL = "!@#$%*-_?".toCharArray();
    private static final char[] ALL = (new String(UPPER) + new String(LOWER) + new String(DIGITS) + new String(SPECIAL)).toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private TemporaryPasswords() {
    }

    public static String generate() {
        char[] password = new char[20];
        password[0] = UPPER[RANDOM.nextInt(UPPER.length)];
        password[1] = LOWER[RANDOM.nextInt(LOWER.length)];
        password[2] = DIGITS[RANDOM.nextInt(DIGITS.length)];
        password[3] = SPECIAL[RANDOM.nextInt(SPECIAL.length)];
        for (int index = 4; index < password.length; index++) {
            password[index] = ALL[RANDOM.nextInt(ALL.length)];
        }
        for (int index = password.length - 1; index > 0; index--) {
            int swap = RANDOM.nextInt(index + 1);
            char current = password[index];
            password[index] = password[swap];
            password[swap] = current;
        }
        return new String(password);
    }
}
