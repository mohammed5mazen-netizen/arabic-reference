package com.mrsoft.arabicreference.linguistics.domain.text;

import java.text.Normalizer;

/**
 * Conservative Arabic normalization for future search and comparison.
 * The original string is returned unchanged beside the normalized copy.
 */
public final class ArabicTextNormalizer {

    public ArabicText normalize(String originalText) {
        if (originalText == null) {
            throw new IllegalArgumentException("originalText must not be null");
        }
        String normalized = Normalizer.normalize(originalText, Normalizer.Form.NFC);
        normalized = mapCodePoints(normalized);
        normalized = collapseWhitespace(normalized);
        return new ArabicText(originalText, normalized);
    }

    private static String mapCodePoints(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        value.codePoints().forEach(codePoint -> appendNormalized(builder, codePoint));
        return builder.toString();
    }

    private static void appendNormalized(StringBuilder builder, int codePoint) {
        if (isRemoved(codePoint)) {
            return;
        }
        if (isAlefVariant(codePoint)) {
            builder.append('\u0627');
            return;
        }
        if (isUnusualSpace(codePoint)) {
            builder.append(' ');
            return;
        }
        builder.appendCodePoint(codePoint);
    }

    private static boolean isRemoved(int codePoint) {
        return codePoint == 0x0640
                || isArabicDiacritic(codePoint)
                || isFormatControl(codePoint);
    }

    private static boolean isArabicDiacritic(int codePoint) {
        return (codePoint >= 0x064B && codePoint <= 0x065F)
                || codePoint == 0x0670
                || (codePoint >= 0x06D6 && codePoint <= 0x06ED);
    }

    private static boolean isFormatControl(int codePoint) {
        return codePoint == 0xFEFF
                || codePoint == 0x200E
                || codePoint == 0x200F
                || (codePoint >= 0x202A && codePoint <= 0x202E)
                || (codePoint >= 0x2066 && codePoint <= 0x2069);
    }

    private static boolean isAlefVariant(int codePoint) {
        return codePoint == 0x0622
                || codePoint == 0x0623
                || codePoint == 0x0625
                || codePoint == 0x0671
                || codePoint == 0x0672
                || codePoint == 0x0673
                || codePoint == 0x0675;
    }

    private static boolean isUnusualSpace(int codePoint) {
        return codePoint == 0x00A0 || codePoint == 0x2007 || codePoint == 0x202F;
    }

    private static String collapseWhitespace(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        boolean pendingSpace = false;
        boolean started = false;
        for (int index = 0; index < value.length();) {
            int codePoint = value.codePointAt(index);
            index += Character.charCount(codePoint);
            if (codePoint == ' ' || Character.isWhitespace(codePoint)) {
                if (started) {
                    pendingSpace = true;
                }
                continue;
            }
            if (pendingSpace) {
                builder.append(' ');
                pendingSpace = false;
            }
            builder.appendCodePoint(codePoint);
            started = true;
        }
        return builder.toString();
    }
}
