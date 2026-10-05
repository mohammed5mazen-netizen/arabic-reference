package com.mrsoft.arabicreference.literature.application;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PublicEra;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PublicFigure;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PublicLink;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PublicWork;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryEraRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryGenreRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiterarySchoolRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkRepository;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiteratureQueryService {

    private final LiteraryEraRepository eras;
    private final LiteraryGenreRepository genres;
    private final LiterarySchoolRepository schools;
    private final LiteraryFigureRepository figures;
    private final LiteraryWorkRepository works;

    public LiteratureQueryService(
            LiteraryEraRepository eras,
            LiteraryGenreRepository genres,
            LiterarySchoolRepository schools,
            LiteraryFigureRepository figures,
            LiteraryWorkRepository works) {
        this.eras = eras;
        this.genres = genres;
        this.schools = schools;
        this.figures = figures;
        this.works = works;
    }

    @Transactional(readOnly = true)
    public List<PublicEra> eras() {
        List<PublicEra> page = new ArrayList<>();
        for (var era : eras.visibleToPublic(PublicationStatus.ARCHIVED)) {
            page.add(era(era.getPublishedSnapshot()));
        }
        return page;
    }

    @Transactional(readOnly = true)
    public PublicEra era(String slug) {
        var era = eras.findBySlug(slug).filter(item -> item.visibleToPublic()).orElseThrow(() -> missing("Era"));
        return era(era.getPublishedSnapshot());
    }

    @Transactional(readOnly = true)
    public List<PublicLink> genres() {
        List<PublicLink> links = new ArrayList<>();
        for (var genre : genres.visibleToPublic(PublicationStatus.ARCHIVED)) {
            Map<String, Object> snapshot = genre.getPublishedSnapshot();
            links.add(new PublicLink(text(snapshot.get("name")), text(snapshot.get("slug")), text(snapshot.get("description"))));
        }
        return links;
    }

    @Transactional(readOnly = true)
    public List<PublicLink> schools() {
        List<PublicLink> links = new ArrayList<>();
        for (var school : schools.visibleToPublic(PublicationStatus.ARCHIVED)) {
            Map<String, Object> snapshot = school.getPublishedSnapshot();
            links.add(new PublicLink(text(snapshot.get("name")), text(snapshot.get("slug")), text(snapshot.get("description"))));
        }
        return links;
    }

    @Transactional(readOnly = true)
    public PublicFigure figure(String slug) {
        var figure = figures.findBySlug(slug).filter(item -> item.visibleToPublic()).orElseThrow(() -> missing("Figure"));
        Map<String, Object> snapshot = figure.getPublishedSnapshot();
        return new PublicFigure(
                text(snapshot.get("name")),
                text(snapshot.get("slug")),
                text(snapshot.get("biography")),
                text(snapshot.get("birthLabel")),
                text(snapshot.get("deathLabel")),
                maps(snapshot.get("aliases")),
                strings(snapshot.get("roles")),
                maps(snapshot.get("eras")),
                maps(snapshot.get("schools")),
                maps(snapshot.get("works")),
                maps(snapshot.get("sources")));
    }

    @Transactional(readOnly = true)
    public PublicWork work(String slug) {
        var work = works.findBySlug(slug).filter(item -> item.visibleToPublic()).orElseThrow(() -> missing("Work"));
        Map<String, Object> snapshot = work.getPublishedSnapshot();
        return new PublicWork(
                text(snapshot.get("title")),
                text(snapshot.get("slug")),
                text(snapshot.get("description")),
                text(snapshot.get("languageCode")),
                map(snapshot.get("genre")),
                map(snapshot.get("era")),
                text(snapshot.get("compositionDisplay")),
                text(snapshot.get("rights")),
                text(snapshot.get("rightsNote")),
                text(snapshot.get("attribution")),
                strings(snapshot.get("aliases")),
                maps(snapshot.get("figures")),
                snapshot.containsKey("excerpts") ? maps(snapshot.get("excerpts")) : null,
                maps(snapshot.get("sources")));
    }

    private static PublicEra era(Map<String, Object> snapshot) {
        return new PublicEra(
                text(snapshot.get("name")),
                text(snapshot.get("slug")),
                text(snapshot.get("startDescription")),
                text(snapshot.get("endDescription")),
                text(snapshot.get("summary")),
                text(snapshot.get("historicalContext")),
                maps(snapshot.get("sources")));
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> raw ? (Map<String, Object>) raw : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> items)) {
            return List.of();
        }
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> raw) {
                maps.add((Map<String, Object>) raw);
            }
        }
        return maps;
    }

    private static List<String> strings(Object value) {
        if (!(value instanceof List<?> items)) {
            return List.of();
        }
        List<String> strings = new ArrayList<>();
        for (Object item : items) {
            if (item != null && !(item instanceof Map<?, ?>)) {
                strings.add(String.valueOf(item));
            }
        }
        return strings;
    }

    private static ResourceNotFoundException missing(String name) {
        return new ResourceNotFoundException(name + " was not found.");
    }
}
