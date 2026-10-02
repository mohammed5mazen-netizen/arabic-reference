package com.mrsoft.arabicreference.morphology.domain;

public final class ArabicLetters {

    private ArabicLetters() {
    }

    public static boolean isLetter(int codePoint) {
        return (codePoint >= 0x0621 && codePoint <= 0x063A) || (codePoint >= 0x0641 && codePoint <= 0x064A);
    }

    public static boolean words(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        boolean letter = false;
        for (int index = 0; index < value.length();) {
            int codePoint = value.codePointAt(index);
            index += Character.charCount(codePoint);
            if (codePoint == ' ') {
                continue;
            }
            if (!isLetter(codePoint)) {
                return false;
            }
            letter = true;
        }
        return letter;
    }

    public static String lettersOnly(String value) {
        StringBuilder builder = new StringBuilder();
        value.codePoints().filter(ArabicLetters::isLetter).forEach(builder::appendCodePoint);
        return builder.toString();
    }
}
