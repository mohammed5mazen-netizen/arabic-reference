package com.mrsoft.arabicreference.linguistics.domain.text;

/**
 * Search key derived from {@link ArabicTextNormalizer} plus lookup-only folding.
 * Display text stays untouched. This class does not change the S0 normalizer.
 * A leading definite article is removed only when at least three letters remain,
 * so short forms such as الله are kept. Hamza carriers ء ؤ ئ and the letters ة ى are not folded.
 * Latin text is not transliterated.
 */
public final class ArabicSearchNormalizer {

    private static final ArabicTextNormalizer BASE = new ArabicTextNormalizer();
    private static final int ARTICLE_REMAINDER = 3;

    private ArabicSearchNormalizer() {
    }

    public static String key(String value) {
        return align(value, true).key();
    }

    /** Folding without definite-article removal, for phrases inside definitions. */
    public static String folded(String value) {
        return align(value, false).key();
    }

    public static Alignment align(String value) {
        return align(value, true);
    }

    public static Alignment align(String value, boolean stripArticle) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        StringBuilder key = new StringBuilder(value.length());
        int[] indexes = new int[value.codePointCount(0, value.length()) + 1];
        int produced = 0;
        boolean pendingSpace = false;
        boolean started = false;
        int codePointIndex = 0;
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.isWhitespace(codePoint) || codePoint == 0x00A0 || codePoint == 0x2007 || codePoint == 0x202F) {
                pendingSpace = started;
                codePointIndex++;
                continue;
            }
            String piece = BASE.normalize(new String(Character.toChars(codePoint))).normalizedText();
            for (int pieceOffset = 0; pieceOffset < piece.length();) {
                int normalized = piece.codePointAt(pieceOffset);
                pieceOffset += Character.charCount(normalized);
                if (Character.isWhitespace(normalized)) {
                    pendingSpace = started;
                    continue;
                }
                int folded = foldDigit(normalized);
                if (isDropped(folded)) {
                    continue;
                }
                if (pendingSpace) {
                    indexes[produced] = codePointIndex;
                    key.append(' ');
                    produced++;
                    pendingSpace = false;
                }
                indexes[produced] = codePointIndex;
                key.appendCodePoint(folded);
                produced++;
                started = true;
            }
            codePointIndex++;
        }
        int drop = stripArticle ? leadingArticleLength(key) : 0;
        if (drop == 0) {
            return new Alignment(key.toString(), copy(indexes, produced));
        }
        int[] kept = new int[produced - drop];
        System.arraycopy(indexes, drop, kept, 0, kept.length);
        return new Alignment(key.substring(key.offsetByCodePoints(0, drop)), kept);
    }

    private static int leadingArticleLength(StringBuilder key) {
        if (key.codePoints().count() < 2L + ARTICLE_REMAINDER) {
            return 0;
        }
        if (key.codePointAt(0) != 'ا' || key.codePointAt(Character.charCount(key.codePointAt(0))) != 'ل') {
            return 0;
        }
        int remainder = (int) key.codePoints().count() - 2;
        boolean arabic = false;
        int seen = 0;
        for (int offset = 0; offset < key.length();) {
            int codePoint = key.codePointAt(offset);
            offset += Character.charCount(codePoint);
            seen++;
            if (seen <= 2) {
                continue;
            }
            if (isArabicLetter(codePoint)) {
                arabic = true;
                break;
            }
        }
        return arabic && remainder >= ARTICLE_REMAINDER ? 2 : 0;
    }

    private static boolean isArabicLetter(int codePoint) {
        return (codePoint >= 0x0621 && codePoint <= 0x063A) || (codePoint >= 0x0641 && codePoint <= 0x064A);
    }

    private static int foldDigit(int codePoint) {
        if (codePoint >= 0x0660 && codePoint <= 0x0669) {
            return '0' + (codePoint - 0x0660);
        }
        if (codePoint >= 0x06F0 && codePoint <= 0x06F9) {
            return '0' + (codePoint - 0x06F0);
        }
        return codePoint;
    }

    private static boolean isDropped(int codePoint) {
        int type = Character.getType(codePoint);
        return type == Character.CONNECTOR_PUNCTUATION
                || type == Character.DASH_PUNCTUATION
                || type == Character.START_PUNCTUATION
                || type == Character.END_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION
                || type == Character.FINAL_QUOTE_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION
                || type == Character.MATH_SYMBOL
                || type == Character.CURRENCY_SYMBOL
                || type == Character.MODIFIER_SYMBOL
                || type == Character.OTHER_SYMBOL;
    }

    private static int[] copy(int[] values, int length) {
        int[] copy = new int[length];
        System.arraycopy(values, 0, copy, 0, length);
        return copy;
    }

    public record Alignment(String key, int[] sourceCodePoint) {
    }
}
