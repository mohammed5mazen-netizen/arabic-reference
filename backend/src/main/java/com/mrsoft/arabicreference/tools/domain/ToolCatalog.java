package com.mrsoft.arabicreference.tools.domain;

import java.util.List;

/**
 * The public tool list. This is a registry, not a plugin framework.
 */
public final class ToolCatalog {

    public record Definition(
            ToolCode code,
            String name,
            String description,
            String route,
            String status,
            String inputKind,
            int maxCodePoints,
            int maxWords) {
    }

    public static final List<Definition> ALL = List.of(
            new Definition(ToolCode.MORPHOLOGY, "المحلل الصرفي", "تحليل صرفي محدود من المحرك المنشور والقواعد المصرّح بها.", "/tools/morphology", "LIMITED", "كلمة", 80, 1),
            new Definition(ToolCode.ROOT, "مستكشف الجذر", "يعرض الجذر الموثّق، أو جذرًا محتملًا إذا كان مستنتجًا بقاعدة.", "/tools/root", "AVAILABLE", "كلمة أو جذر", 40, 1),
            new Definition(ToolCode.DERIVATIONS, "مستكشف المشتقات", "شبكة المشتقات المنشورة فقط، بلا توليد كلمات جديدة.", "/tools/derivations", "AVAILABLE", "كلمة أو جذر", 40, 1),
            new Definition(ToolCode.PATTERNS, "مستكشف الأوزان", "الأوزان المسجّلة وأمثلة منشورة مرتبطة بها.", "/tools/patterns", "AVAILABLE", "وزن أو كلمة", 40, 1),
            new Definition(ToolCode.WORD_ANALYSIS, "محلل الكلمة", "يجمع ما هو منشور عن كلمة واحدة، ولا يولّد معنى.", "/tools/word-analysis", "AVAILABLE", "كلمة", 40, 1),
            new Definition(ToolCode.COMPARE, "مقارنة الكلمات", "يعرض البيانات المنشورة لكلمتين جنبًا إلى جنب.", "/tools/compare", "AVAILABLE", "كلمتان", 40, 1),
            new Definition(ToolCode.RELATIONS, "المرادفات والأضداد", "علاقات كل معنى على حدة.", "/tools/relations", "AVAILABLE", "كلمة", 40, 1),
            new Definition(ToolCode.SPELLING_CHECK, "التحقق الإملائي المرجعي", "يطابق الصيغة مع المعجم والأخطاء الشائعة المسجّلة. ليس مدققًا آليًا.", "/tools/spelling-check", "LIMITED", "كلمة أو عبارة قصيرة", 80, 6),
            new Definition(ToolCode.GRAMMAR, "مستكشف القواعد النحوية", "يبحث في الموضوعات والقواعد المنشورة، ولا يعرب الجمل.", "/tools/grammar", "AVAILABLE", "مصطلح", 80, 6),
            new Definition(ToolCode.EXPLORE, "مستكشف العلاقات اللغوية", "رسم محدود للعلاقات المنشورة، مع قائمة نصية.", "/tools/explore", "AVAILABLE", "كلمة", 40, 1));

    private ToolCatalog() {
    }

    public static Definition require(ToolCode code) {
        return ALL.stream().filter(item -> item.code() == code).findFirst().orElseThrow();
    }
}
