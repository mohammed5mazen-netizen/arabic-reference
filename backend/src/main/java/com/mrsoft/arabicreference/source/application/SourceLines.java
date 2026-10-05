package com.mrsoft.arabicreference.source.application;

import com.mrsoft.arabicreference.source.application.SourceViews.CitationView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SourceLines {

    private SourceLines() {
    }

    public static List<Map<String, Object>> of(List<CitationView> views) {
        List<Map<String, Object>> lines = new ArrayList<>();
        for (CitationView view : views) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("title", view.title());
            line.put("author", view.author());
            line.put("attribution", view.attributionText());
            line.put("pageFrom", view.pageFrom());
            line.put("pageTo", view.pageTo());
            line.put("volume", view.volume());
            line.put("chapter", view.chapter());
            line.put("poem", view.poem());
            line.put("verse", view.verse());
            lines.add(line);
        }
        return lines;
    }
}
