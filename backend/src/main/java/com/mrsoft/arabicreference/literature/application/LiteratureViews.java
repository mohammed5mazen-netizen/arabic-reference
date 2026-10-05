package com.mrsoft.arabicreference.literature.application;

import com.mrsoft.arabicreference.linguistics.domain.time.CalendarSystem;
import com.mrsoft.arabicreference.linguistics.domain.time.DatePrecision;
import com.mrsoft.arabicreference.literature.domain.FigureAliasKind;
import com.mrsoft.arabicreference.literature.domain.LiteraryRole;
import com.mrsoft.arabicreference.literature.domain.WorkRights;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LiteratureViews {

    private LiteratureViews() {
    }

    public record PageResult<T>(List<T> items, int page, int size, long total) {
    }

    public record DateInput(DatePrecision precision, CalendarSystem calendar, Integer year, LocalDate exactDate, String displayText, Boolean circa) {
    }

    public record DateView(String precision, String calendar, Integer year, LocalDate exactDate, String displayText, boolean circa, String label) {
    }

    public record EraDraft(String name, String startDescription, String endDescription, String summary, String historicalContext, Integer displayOrder) {
    }

    public record EraUpdate(long version, String name, String startDescription, String endDescription, String summary, String historicalContext, Integer displayOrder) {
    }

    public record GenreDraft(String name, String description) {
    }

    public record GenreUpdate(long version, String name, String description) {
    }

    public record SchoolDraft(String name, String description, UUID eraId) {
    }

    public record SchoolUpdate(long version, String name, String description, UUID eraId) {
    }

    public record FigureDraft(String canonicalName, String biographySummary, DateInput birth, DateInput death) {
    }

    public record FigureUpdate(long version, String canonicalName, String biographySummary, DateInput birth, DateInput death) {
    }

    public record AliasDraft(long version, String alias, FigureAliasKind kind) {
    }

    public record RoleDraft(long version, LiteraryRole role) {
    }

    public record EraLinkDraft(long version, UUID eraId) {
    }

    public record SchoolLinkDraft(long version, UUID schoolId) {
    }

    public record WorkDraft(String title, String description, String languageCode, UUID genreId, UUID eraId, String compositionDisplay, String attribution) {
    }

    public record WorkUpdate(long version, String title, String description, String languageCode, UUID genreId, UUID eraId, String compositionDisplay, String attribution) {
    }

    public record WorkAliasDraft(long version, String alias) {
    }

    public record WorkFigureDraft(long version, UUID figureId, String roleLabel) {
    }

    public record RightsUpdate(long version, WorkRights rights, String rightsNote) {
    }

    public record ExcerptDraft(long version, String text, UUID citationId) {
    }

    public record VersionRequest(long version) {
    }

    public record ReasonRequest(long version, String reason) {
    }

    public record CitationRequest(long version, UUID citationId) {
    }

    public record CatalogSummary(UUID id, String name, String slug, String status, long version) {
    }

    public record EraSummary(UUID id, String name, String slug, String status, int displayOrder, long version) {
    }

    public record WorkSummary(UUID id, String title, String slug, String status, long version) {
    }

    public record AliasView(UUID id, String alias, String kind) {
    }

    public record EraLinkView(UUID eraId, String name, String slug) {
    }

    public record SchoolLinkView(UUID schoolId, String name, String slug) {
    }

    public record WorkLinkView(UUID workId, String title, String slug, String roleLabel) {
    }

    public record FigureLinkView(UUID figureId, String name, String slug, String roleLabel) {
    }

    public record ExcerptView(UUID id, String text, UUID citationId, int displayOrder) {
    }

    public record EraAdmin(
            UUID id,
            String name,
            String slug,
            String startDescription,
            String endDescription,
            String summary,
            String historicalContext,
            int displayOrder,
            String status,
            long version,
            List<UUID> citationIds) {
    }

    public record GenreAdmin(UUID id, String name, String slug, String description, String status, long version, List<UUID> citationIds) {
    }

    public record SchoolAdmin(UUID id, UUID eraId, String name, String slug, String description, String status, long version, List<UUID> citationIds) {
    }

    public record FigureAdmin(
            UUID id,
            String canonicalName,
            String slug,
            String biographySummary,
            DateView birth,
            DateView death,
            String status,
            long version,
            List<AliasView> aliases,
            List<String> roles,
            List<EraLinkView> eras,
            List<SchoolLinkView> schools,
            List<WorkLinkView> works,
            List<UUID> citationIds) {
    }

    public record WorkAdmin(
            UUID id,
            String title,
            String slug,
            String description,
            String languageCode,
            UUID genreId,
            UUID eraId,
            String compositionDisplay,
            String rights,
            String rightsNote,
            String attribution,
            String status,
            long version,
            List<String> aliases,
            List<FigureLinkView> figures,
            List<ExcerptView> excerpts,
            List<UUID> citationIds) {
    }

    public record ReviewItem(String kind, UUID id, String title, String status, long version) {
    }

    public record PublicLink(String title, String slug, String summary) {
    }

    public record PublicEra(
            String name,
            String slug,
            String startDescription,
            String endDescription,
            String summary,
            String historicalContext,
            List<Map<String, Object>> sources) {
    }

    public record PublicFigure(
            String name,
            String slug,
            String biography,
            String birthLabel,
            String deathLabel,
            List<Map<String, Object>> aliases,
            List<String> roles,
            List<Map<String, Object>> eras,
            List<Map<String, Object>> schools,
            List<Map<String, Object>> works,
            List<Map<String, Object>> sources) {
    }

    public record PublicWork(
            String title,
            String slug,
            String description,
            String languageCode,
            Map<String, Object> genre,
            Map<String, Object> era,
            String compositionDisplay,
            String rights,
            String rightsNote,
            String attribution,
            List<String> aliases,
            List<Map<String, Object>> figures,
            List<Map<String, Object>> excerpts,
            List<Map<String, Object>> sources) {
    }
}
