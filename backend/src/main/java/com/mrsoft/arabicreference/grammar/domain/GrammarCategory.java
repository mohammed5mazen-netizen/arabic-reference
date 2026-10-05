package com.mrsoft.arabicreference.grammar.domain;

public enum GrammarCategory {
    FOUNDATIONS("أسس النحو"),
    NOMINAL_SENTENCE("الجملة الاسمية"),
    VERBAL_SENTENCE("الجملة الفعلية"),
    MARFUAT("المرفوعات"),
    MANSUBAT("المنصوبات"),
    MAJRURAT("المجرورات"),
    TAWABI("التوابع"),
    NAWASIKH("النواسخ"),
    ASALIB("الأساليب"),
    NUMERALS("العدد"),
    OTHER("أخرى");

    private final String arabicLabel;

    GrammarCategory(String arabicLabel) {
        this.arabicLabel = arabicLabel;
    }

    public String arabicLabel() {
        return arabicLabel;
    }
}
