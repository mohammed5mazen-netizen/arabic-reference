package com.mrsoft.arabicreference.literature.application;

import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.EditorialStore;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ContentSlugs;
import com.mrsoft.arabicreference.linguistics.domain.text.KnowledgeText;
import com.mrsoft.arabicreference.linguistics.domain.time.HistoricalDate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.AliasDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.AliasView;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.CatalogSummary;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.DateInput;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.DateView;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraLinkDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraLinkView;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraSummary;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.EraUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.ExcerptDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.ExcerptView;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.FigureAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.FigureDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.FigureLinkView;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.FigureUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.GenreAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.GenreDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.GenreUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.PageResult;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.ReviewItem;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.RightsUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.RoleDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.SchoolAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.SchoolDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.SchoolLinkDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.SchoolLinkView;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.SchoolUpdate;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkAdmin;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkAliasDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkFigureDraft;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkLinkView;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkSummary;
import com.mrsoft.arabicreference.literature.application.LiteratureViews.WorkUpdate;
import com.mrsoft.arabicreference.literature.domain.ExcerptRules;
import com.mrsoft.arabicreference.literature.domain.WorkRights;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryEraCitationEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryEraCitationRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryEraEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryEraRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryExcerptEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryExcerptRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureAliasEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureAliasRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureCitationEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureCitationRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureEraEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureEraRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureRoleEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureRoleRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryGenreCitationEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryGenreCitationRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryGenreEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryGenreRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiterarySchoolCitationEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiterarySchoolCitationRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiterarySchoolEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiterarySchoolFigureEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiterarySchoolFigureRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiterarySchoolRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkAliasEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkAliasRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkCitationEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkCitationRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkFigureEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkFigureRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkRepository;
import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationEntity;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.source.application.CitationChecks;
import com.mrsoft.arabicreference.source.application.SourceLines;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiteratureAdminService {

    static final String ERA = "literary_era";
    static final String GENRE = "literary_genre";
    static final String SCHOOL = "literary_school";
    static final String FIGURE = "literary_figure";
    static final String WORK = "literary_work";

    private final LiteraryEraRepository eras;
    private final LiteraryGenreRepository genres;
    private final LiterarySchoolRepository schools;
    private final LiteraryFigureRepository figures;
    private final LiteraryWorkRepository works;
    private final LiteraryFigureAliasRepository aliases;
    private final LiteraryFigureRoleRepository roles;
    private final LiteraryFigureEraRepository figureEras;
    private final LiterarySchoolFigureRepository schoolFigures;
    private final LiteraryWorkAliasRepository workAliases;
    private final LiteraryWorkFigureRepository workFigures;
    private final LiteraryExcerptRepository excerpts;
    private final LiteraryEraCitationRepository eraCitations;
    private final LiteraryGenreCitationRepository genreCitations;
    private final LiterarySchoolCitationRepository schoolCitations;
    private final LiteraryFigureCitationRepository figureCitations;
    private final LiteraryWorkCitationRepository workCitations;
    private final EditorialStore editorial;
    private final AuthorizationService authorization;
    private final CitationChecks citations;
    private final LiteratureSearchIndexer search;

    public LiteratureAdminService(
            LiteraryEraRepository eras,
            LiteraryGenreRepository genres,
            LiterarySchoolRepository schools,
            LiteraryFigureRepository figures,
            LiteraryWorkRepository works,
            LiteraryFigureAliasRepository aliases,
            LiteraryFigureRoleRepository roles,
            LiteraryFigureEraRepository figureEras,
            LiterarySchoolFigureRepository schoolFigures,
            LiteraryWorkAliasRepository workAliases,
            LiteraryWorkFigureRepository workFigures,
            LiteraryExcerptRepository excerpts,
            LiteraryEraCitationRepository eraCitations,
            LiteraryGenreCitationRepository genreCitations,
            LiterarySchoolCitationRepository schoolCitations,
            LiteraryFigureCitationRepository figureCitations,
            LiteraryWorkCitationRepository workCitations,
            EditorialStore editorial,
            AuthorizationService authorization,
            CitationChecks citations,
            LiteratureSearchIndexer search) {
        this.eras = eras;
        this.genres = genres;
        this.schools = schools;
        this.figures = figures;
        this.works = works;
        this.aliases = aliases;
        this.roles = roles;
        this.figureEras = figureEras;
        this.schoolFigures = schoolFigures;
        this.workAliases = workAliases;
        this.workFigures = workFigures;
        this.excerpts = excerpts;
        this.eraCitations = eraCitations;
        this.genreCitations = genreCitations;
        this.schoolCitations = schoolCitations;
        this.figureCitations = figureCitations;
        this.workCitations = workCitations;
        this.editorial = editorial;
        this.authorization = authorization;
        this.citations = citations;
        this.search = search;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public PageResult<EraSummary> eras(int page, int size) {
        var result = eras.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(era -> new EraSummary(era.getId(), era.getNameOriginal(), era.getSlug(), era.getStatus().name(), era.getDisplayOrder(), era.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public EraAdmin era(UUID id) {
        return eraAdmin(eras.findById(id).orElseThrow(() -> missing("Era")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public EraAdmin createEra(EraDraft draft) {
        UUID actor = authorization.requireAccess().userId();
        LiteraryEraEntity era = new LiteraryEraEntity();
        editorial.prepareNew(era, actor);
        applyEra(era, draft.name(), draft.startDescription(), draft.endDescription(), draft.summary(), draft.historicalContext(), draft.displayOrder());
        era.setSlug(ContentSlugs.of(era.getNameNormalized(), era.getId()));
        editorial.persist(eras, era, actor, AuditEventType.LITERARY_ERA_CREATED, ERA);
        return eraAdmin(era);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public EraAdmin updateEra(UUID id, EraUpdate update) {
        LiteraryEraEntity era = lockedEra(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(era, actor, "era update", ERA);
        applyEra(era, update.name(), update.startDescription(), update.endDescription(), update.summary(), update.historicalContext(), update.displayOrder());
        editorial.persist(eras, era, actor, AuditEventType.LITERARY_ERA_CREATED, ERA);
        return eraAdmin(era);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public EraAdmin citeEra(UUID id, long version, UUID citationId) {
        citations.requireExisting(citationId);
        LiteraryEraEntity era = lockedEra(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.open(era, actor, "citation linked", ERA);
        if (!eraCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            eraCitations.save(new LiteraryEraCitationEntity(id, citationId));
        }
        editorial.persist(eras, era, actor, AuditEventType.CITATION_ADDED, ERA);
        return eraAdmin(era);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public EraAdmin submitEra(UUID id, long version) {
        return moveEra(id, version, "submit");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public EraAdmin verifyEra(UUID id, long version) {
        return moveEra(id, version, "verify");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public EraAdmin requestEraChanges(UUID id, long version, String reason) {
        LiteraryEraEntity era = lockedEra(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.requestChanges(era, actor, KnowledgeText.required(reason, "reason", 500));
        editorial.persist(eras, era, actor, AuditEventType.LITERARY_CONTENT_CHANGES_REQUESTED, ERA);
        return eraAdmin(era);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public EraAdmin publishEra(UUID id, long version) {
        search.lock();
        LiteraryEraEntity era = lockedEra(id, version);
        UUID actor = authorization.requireAccess().userId();
        var lines = sourcesOf(ids(eraCitations.findByOwnerId(id)));
        editorial.publish(era, actor);
        era.setPublishedSnapshot(eraSnapshot(era, lines));
        editorial.persist(eras, era, actor, AuditEventType.LITERARY_ERA_PUBLISHED, ERA);
        search.onEraPublished(era);
        return eraAdmin(era);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public EraAdmin archiveEra(UUID id, long version) {
        search.lock();
        LiteraryEraEntity era = lockedEra(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(era);
        editorial.persist(eras, era, actor, AuditEventType.LITERARY_CONTENT_ARCHIVED, ERA);
        search.onEraArchived(id);
        return eraAdmin(era);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public PageResult<CatalogSummary> genres(int page, int size) {
        var result = genres.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(genre -> new CatalogSummary(genre.getId(), genre.getNameOriginal(), genre.getSlug(), genre.getStatus().name(), genre.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public GenreAdmin genre(UUID id) {
        return genreAdmin(genres.findById(id).orElseThrow(() -> missing("Genre")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public GenreAdmin createGenre(GenreDraft draft) {
        UUID actor = authorization.requireAccess().userId();
        LiteraryGenreEntity genre = new LiteraryGenreEntity();
        editorial.prepareNew(genre, actor);
        applyGenre(genre, draft.name(), draft.description());
        genre.setSlug(ContentSlugs.of(genre.getNameNormalized(), genre.getId()));
        editorial.persist(genres, genre, actor, AuditEventType.LITERARY_GENRE_PUBLISHED, GENRE);
        return genreAdmin(genre);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public GenreAdmin updateGenre(UUID id, GenreUpdate update) {
        LiteraryGenreEntity genre = lockedGenre(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(genre, actor, "genre update", GENRE);
        applyGenre(genre, update.name(), update.description());
        editorial.persist(genres, genre, actor, AuditEventType.LITERARY_GENRE_PUBLISHED, GENRE);
        return genreAdmin(genre);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public GenreAdmin citeGenre(UUID id, long version, UUID citationId) {
        citations.requireExisting(citationId);
        LiteraryGenreEntity genre = lockedGenre(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.open(genre, actor, "citation linked", GENRE);
        if (!genreCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            genreCitations.save(new LiteraryGenreCitationEntity(id, citationId));
        }
        editorial.persist(genres, genre, actor, AuditEventType.CITATION_ADDED, GENRE);
        return genreAdmin(genre);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public GenreAdmin submitGenre(UUID id, long version) {
        return moveGenre(id, version, "submit");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public GenreAdmin verifyGenre(UUID id, long version) {
        return moveGenre(id, version, "verify");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public GenreAdmin requestGenreChanges(UUID id, long version, String reason) {
        LiteraryGenreEntity genre = lockedGenre(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.requestChanges(genre, actor, KnowledgeText.required(reason, "reason", 500));
        editorial.persist(genres, genre, actor, AuditEventType.LITERARY_CONTENT_CHANGES_REQUESTED, GENRE);
        return genreAdmin(genre);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public GenreAdmin publishGenre(UUID id, long version) {
        search.lock();
        LiteraryGenreEntity genre = lockedGenre(id, version);
        UUID actor = authorization.requireAccess().userId();
        var lines = sourcesOf(ids(genreCitations.findByOwnerId(id)));
        editorial.publish(genre, actor);
        genre.setPublishedSnapshot(genreSnapshot(genre, lines));
        editorial.persist(genres, genre, actor, AuditEventType.LITERARY_GENRE_PUBLISHED, GENRE);
        return genreAdmin(genre);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public GenreAdmin archiveGenre(UUID id, long version) {
        search.lock();
        LiteraryGenreEntity genre = lockedGenre(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(genre);
        editorial.persist(genres, genre, actor, AuditEventType.LITERARY_CONTENT_ARCHIVED, GENRE);
        return genreAdmin(genre);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public PageResult<CatalogSummary> schools(int page, int size) {
        var result = schools.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(school -> new CatalogSummary(school.getId(), school.getNameOriginal(), school.getSlug(), school.getStatus().name(), school.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public SchoolAdmin school(UUID id) {
        return schoolAdmin(schools.findById(id).orElseThrow(() -> missing("School")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public SchoolAdmin createSchool(SchoolDraft draft) {
        UUID actor = authorization.requireAccess().userId();
        LiterarySchoolEntity school = new LiterarySchoolEntity();
        editorial.prepareNew(school, actor);
        applySchool(school, draft.name(), draft.description(), draft.eraId());
        school.setSlug(ContentSlugs.of(school.getNameNormalized(), school.getId()));
        editorial.persist(schools, school, actor, AuditEventType.LITERARY_SCHOOL_PUBLISHED, SCHOOL);
        return schoolAdmin(school);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public SchoolAdmin updateSchool(UUID id, SchoolUpdate update) {
        LiterarySchoolEntity school = lockedSchool(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(school, actor, "school update", SCHOOL);
        applySchool(school, update.name(), update.description(), update.eraId());
        editorial.persist(schools, school, actor, AuditEventType.LITERARY_SCHOOL_PUBLISHED, SCHOOL);
        return schoolAdmin(school);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public SchoolAdmin citeSchool(UUID id, long version, UUID citationId) {
        citations.requireExisting(citationId);
        LiterarySchoolEntity school = lockedSchool(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.open(school, actor, "citation linked", SCHOOL);
        if (!schoolCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            schoolCitations.save(new LiterarySchoolCitationEntity(id, citationId));
        }
        editorial.persist(schools, school, actor, AuditEventType.CITATION_ADDED, SCHOOL);
        return schoolAdmin(school);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public SchoolAdmin submitSchool(UUID id, long version) {
        return moveSchool(id, version, "submit");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public SchoolAdmin verifySchool(UUID id, long version) {
        return moveSchool(id, version, "verify");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public SchoolAdmin requestSchoolChanges(UUID id, long version, String reason) {
        LiterarySchoolEntity school = lockedSchool(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.requestChanges(school, actor, KnowledgeText.required(reason, "reason", 500));
        editorial.persist(schools, school, actor, AuditEventType.LITERARY_CONTENT_CHANGES_REQUESTED, SCHOOL);
        return schoolAdmin(school);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public SchoolAdmin publishSchool(UUID id, long version) {
        search.lock();
        LiterarySchoolEntity school = lockedSchool(id, version);
        UUID actor = authorization.requireAccess().userId();
        var lines = sourcesOf(ids(schoolCitations.findByOwnerId(id)));
        editorial.publish(school, actor);
        school.setPublishedSnapshot(schoolSnapshot(school, lines));
        editorial.persist(schools, school, actor, AuditEventType.LITERARY_SCHOOL_PUBLISHED, SCHOOL);
        return schoolAdmin(school);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public SchoolAdmin archiveSchool(UUID id, long version) {
        search.lock();
        LiterarySchoolEntity school = lockedSchool(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(school);
        editorial.persist(schools, school, actor, AuditEventType.LITERARY_CONTENT_ARCHIVED, SCHOOL);
        return schoolAdmin(school);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public PageResult<CatalogSummary> figures(int page, int size) {
        var result = figures.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(figure -> new CatalogSummary(figure.getId(), figure.getCanonicalName(), figure.getSlug(), figure.getStatus().name(), figure.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public FigureAdmin figure(UUID id) {
        return figureAdmin(figures.findById(id).orElseThrow(() -> missing("Figure")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public FigureAdmin createFigure(FigureDraft draft) {
        UUID actor = authorization.requireAccess().userId();
        LiteraryFigureEntity figure = new LiteraryFigureEntity();
        editorial.prepareNew(figure, actor);
        applyFigure(figure, draft.canonicalName(), draft.biographySummary(), draft.birth(), draft.death());
        figure.setSlug(ContentSlugs.of(figure.getNormalizedName(), figure.getId()));
        editorial.persist(figures, figure, actor, AuditEventType.LITERARY_FIGURE_CREATED, FIGURE);
        return figureAdmin(figure);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public FigureAdmin updateFigure(UUID id, FigureUpdate update) {
        LiteraryFigureEntity figure = lockedFigure(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(figure, actor, "figure update", FIGURE);
        applyFigure(figure, update.canonicalName(), update.biographySummary(), update.birth(), update.death());
        editorial.persist(figures, figure, actor, AuditEventType.LITERARY_FIGURE_CREATED, FIGURE);
        return figureAdmin(figure);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public FigureAdmin addAlias(UUID id, AliasDraft draft) {
        String alias = KnowledgeText.required(draft.alias(), "alias", 160);
        String normalized = KnowledgeText.normalized(alias);
        if (draft.kind() == null) {
            throw invalid("kind", "Choose an alias kind.");
        }
        LiteraryFigureEntity figure = lockedFigure(id, draft.version());
        if (aliases.existsByFigureIdAndNormalized(id, normalized)) {
            throw new ConflictException("This figure already uses that alias.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(figure, actor, "alias added", FIGURE);
        LiteraryFigureAliasEntity row = new LiteraryFigureAliasEntity();
        row.setId(Ids.random());
        row.setFigureId(id);
        row.setAlias(alias);
        row.setNormalized(normalized);
        row.setKind(draft.kind());
        aliases.save(row);
        editorial.persist(figures, figure, actor, AuditEventType.LITERARY_FIGURE_CREATED, FIGURE);
        return figureAdmin(figure);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public FigureAdmin addRole(UUID id, RoleDraft draft) {
        if (draft.role() == null) {
            throw invalid("role", "Choose a role.");
        }
        LiteraryFigureEntity figure = lockedFigure(id, draft.version());
        if (roles.existsByFigureIdAndRole(id, draft.role())) {
            throw new ConflictException("This figure already has that role.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(figure, actor, "role added", FIGURE);
        roles.save(new LiteraryFigureRoleEntity(id, draft.role()));
        editorial.persist(figures, figure, actor, AuditEventType.LITERARY_FIGURE_CREATED, FIGURE);
        return figureAdmin(figure);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public FigureAdmin linkEra(UUID id, EraLinkDraft draft) {
        requireEra(draft.eraId());
        LiteraryFigureEntity figure = lockedFigure(id, draft.version());
        if (figureEras.existsByFigureIdAndEraId(id, draft.eraId())) {
            throw new ConflictException("This figure is already linked to that era.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(figure, actor, "era linked", FIGURE);
        figureEras.save(new LiteraryFigureEraEntity(id, draft.eraId()));
        editorial.persist(figures, figure, actor, AuditEventType.LITERARY_FIGURE_CREATED, FIGURE);
        return figureAdmin(figure);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public FigureAdmin linkSchool(UUID id, SchoolLinkDraft draft) {
        requireSchool(draft.schoolId());
        LiteraryFigureEntity figure = lockedFigure(id, draft.version());
        if (schoolFigures.existsBySchoolIdAndFigureId(draft.schoolId(), id)) {
            throw new ConflictException("This figure is already linked to that school.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(figure, actor, "school linked", FIGURE);
        schoolFigures.save(new LiterarySchoolFigureEntity(draft.schoolId(), id));
        editorial.persist(figures, figure, actor, AuditEventType.LITERARY_FIGURE_CREATED, FIGURE);
        return figureAdmin(figure);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public FigureAdmin citeFigure(UUID id, long version, UUID citationId) {
        citations.requireExisting(citationId);
        LiteraryFigureEntity figure = lockedFigure(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.open(figure, actor, "citation linked", FIGURE);
        if (!figureCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            figureCitations.save(new LiteraryFigureCitationEntity(id, citationId));
        }
        editorial.persist(figures, figure, actor, AuditEventType.CITATION_ADDED, FIGURE);
        return figureAdmin(figure);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_FIGURE_MANAGE + "')")
    public FigureAdmin submitFigure(UUID id, long version) {
        return moveFigure(id, version, "submit");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public FigureAdmin verifyFigure(UUID id, long version) {
        return moveFigure(id, version, "verify");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public FigureAdmin requestFigureChanges(UUID id, long version, String reason) {
        LiteraryFigureEntity figure = lockedFigure(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.requestChanges(figure, actor, KnowledgeText.required(reason, "reason", 500));
        editorial.persist(figures, figure, actor, AuditEventType.LITERARY_CONTENT_CHANGES_REQUESTED, FIGURE);
        return figureAdmin(figure);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public FigureAdmin publishFigure(UUID id, long version) {
        search.lock();
        LiteraryFigureEntity figure = lockedFigure(id, version);
        UUID actor = authorization.requireAccess().userId();
        var lines = sourcesOf(ids(figureCitations.findByOwnerId(id)));
        editorial.publish(figure, actor);
        figure.setPublishedSnapshot(figureSnapshot(figure, lines));
        editorial.persist(figures, figure, actor, AuditEventType.LITERARY_FIGURE_PUBLISHED, FIGURE);
        search.onFigurePublished(figure);
        return figureAdmin(figure);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public FigureAdmin archiveFigure(UUID id, long version) {
        search.lock();
        LiteraryFigureEntity figure = lockedFigure(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(figure);
        editorial.persist(figures, figure, actor, AuditEventType.LITERARY_CONTENT_ARCHIVED, FIGURE);
        search.onFigureArchived(id);
        return figureAdmin(figure);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public PageResult<WorkSummary> works(int page, int size) {
        var result = works.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(work -> new WorkSummary(work.getId(), work.getTitleOriginal(), work.getSlug(), work.getStatus().name(), work.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_VIEW + "')")
    public WorkAdmin work(UUID id) {
        return workAdmin(works.findById(id).orElseThrow(() -> missing("Work")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_WORK_MANAGE + "')")
    public WorkAdmin createWork(WorkDraft draft) {
        UUID actor = authorization.requireAccess().userId();
        LiteraryWorkEntity work = new LiteraryWorkEntity();
        editorial.prepareNew(work, actor);
        applyWork(work, draft.title(), draft.description(), draft.languageCode(), draft.genreId(), draft.eraId(), draft.compositionDisplay(), draft.attribution());
        work.setRightsStatus(WorkRights.UNKNOWN);
        work.setSlug(ContentSlugs.of(work.getTitleNormalized(), work.getId()));
        editorial.persist(works, work, actor, AuditEventType.LITERARY_WORK_CREATED, WORK);
        return workAdmin(work);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_WORK_MANAGE + "')")
    public WorkAdmin updateWork(UUID id, WorkUpdate update) {
        LiteraryWorkEntity work = lockedWork(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(work, actor, "work update", WORK);
        applyWork(work, update.title(), update.description(), update.languageCode(), update.genreId(), update.eraId(), update.compositionDisplay(), update.attribution());
        editorial.persist(works, work, actor, AuditEventType.LITERARY_WORK_CREATED, WORK);
        return workAdmin(work);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_WORK_MANAGE + "')")
    public WorkAdmin addWorkAlias(UUID id, WorkAliasDraft draft) {
        String alias = KnowledgeText.required(draft.alias(), "alias", 200);
        String normalized = KnowledgeText.normalized(alias);
        LiteraryWorkEntity work = lockedWork(id, draft.version());
        if (workAliases.existsByWorkIdAndNormalized(id, normalized)) {
            throw new ConflictException("This work already uses that alias.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(work, actor, "alias added", WORK);
        LiteraryWorkAliasEntity row = new LiteraryWorkAliasEntity();
        row.setId(Ids.random());
        row.setWorkId(id);
        row.setAlias(alias);
        row.setNormalized(normalized);
        workAliases.save(row);
        editorial.persist(works, work, actor, AuditEventType.LITERARY_WORK_CREATED, WORK);
        return workAdmin(work);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_WORK_MANAGE + "')")
    public WorkAdmin linkFigure(UUID id, WorkFigureDraft draft) {
        requireFigure(draft.figureId());
        LiteraryWorkEntity work = lockedWork(id, draft.version());
        if (workFigures.existsByWorkIdAndFigureId(id, draft.figureId())) {
            throw new ConflictException("This work is already linked to that figure.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(work, actor, "figure linked", WORK);
        workFigures.save(new LiteraryWorkFigureEntity(id, draft.figureId(), KnowledgeText.optional(draft.roleLabel(), "roleLabel", 80)));
        editorial.persist(works, work, actor, AuditEventType.LITERARY_WORK_CREATED, WORK);
        return workAdmin(work);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_RIGHTS_MANAGE + "')")
    public WorkAdmin setRights(UUID id, RightsUpdate update) {
        if (update.rights() == null) {
            throw invalid("rights", "Choose a rights status.");
        }
        String note = KnowledgeText.optional(update.rightsNote(), "rightsNote", 1000);
        LiteraryWorkEntity work = lockedWork(id, update.version());
        if (!update.rights().allowsExcerpt() && !excerpts.findByWorkIdOrderByDisplayOrderAsc(id).isEmpty()) {
            throw new ConflictException("These rights do not allow excerpts, and this work already has excerpts.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(work, actor, "rights updated", WORK);
        work.setRightsStatus(update.rights());
        work.setRightsNote(note);
        editorial.persist(works, work, actor, AuditEventType.LITERARY_WORK_CREATED, WORK);
        return workAdmin(work);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_RIGHTS_MANAGE + "')")
    public WorkAdmin addExcerpt(UUID id, ExcerptDraft draft) {
        LiteraryWorkEntity work = lockedWork(id, draft.version());
        String text = draft.text() == null ? "" : draft.text().trim();
        ExcerptRules.check(work.getRightsStatus(), text, draft.citationId());
        citations.requireExisting(draft.citationId());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(work, actor, "excerpt added", WORK);
        LiteraryExcerptEntity excerpt = new LiteraryExcerptEntity();
        excerpt.setId(Ids.random());
        excerpt.setWorkId(id);
        excerpt.setExcerptText(text);
        excerpt.setCitationId(draft.citationId());
        excerpt.setDisplayOrder(excerpts.findByWorkIdOrderByDisplayOrderAsc(id).size());
        excerpts.save(excerpt);
        editorial.persist(works, work, actor, AuditEventType.LITERARY_WORK_CREATED, WORK);
        return workAdmin(work);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public WorkAdmin citeWork(UUID id, long version, UUID citationId) {
        citations.requireExisting(citationId);
        LiteraryWorkEntity work = lockedWork(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.open(work, actor, "citation linked", WORK);
        if (!workCitations.existsByOwnerIdAndCitationId(id, citationId)) {
            workCitations.save(new LiteraryWorkCitationEntity(id, citationId));
        }
        editorial.persist(works, work, actor, AuditEventType.CITATION_ADDED, WORK);
        return workAdmin(work);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_WORK_MANAGE + "')")
    public WorkAdmin submitWork(UUID id, long version) {
        return moveWork(id, version, "submit");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public WorkAdmin verifyWork(UUID id, long version) {
        return moveWork(id, version, "verify");
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public WorkAdmin requestWorkChanges(UUID id, long version, String reason) {
        LiteraryWorkEntity work = lockedWork(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.requestChanges(work, actor, KnowledgeText.required(reason, "reason", 500));
        editorial.persist(works, work, actor, AuditEventType.LITERARY_CONTENT_CHANGES_REQUESTED, WORK);
        return workAdmin(work);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public WorkAdmin publishWork(UUID id, long version) {
        search.lock();
        LiteraryWorkEntity work = lockedWork(id, version);
        UUID actor = authorization.requireAccess().userId();
        List<LiteraryExcerptEntity> samples = excerpts.findByWorkIdOrderByDisplayOrderAsc(id);
        if (!work.getRightsStatus().allowsExcerpt() && !samples.isEmpty()) {
            throw new ConflictException("These rights do not allow excerpts, and this work already has excerpts.");
        }
        List<UUID> citationIds = new ArrayList<>(ids(workCitations.findByOwnerId(id)));
        for (LiteraryExcerptEntity excerpt : samples) {
            citationIds.add(excerpt.getCitationId());
        }
        var lines = sourcesOf(citationIds);
        editorial.publish(work, actor);
        work.setPublishedSnapshot(workSnapshot(work, samples, lines));
        editorial.persist(works, work, actor, AuditEventType.LITERARY_WORK_PUBLISHED, WORK);
        search.onWorkPublished(work);
        return workAdmin(work);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_PUBLISH + "')")
    public WorkAdmin archiveWork(UUID id, long version) {
        search.lock();
        LiteraryWorkEntity work = lockedWork(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(work);
        editorial.persist(works, work, actor, AuditEventType.LITERARY_CONTENT_ARCHIVED, WORK);
        search.onWorkArchived(id);
        return workAdmin(work);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.LITERATURE_REVIEW + "')")
    public List<ReviewItem> reviewQueue() {
        List<ReviewItem> items = new ArrayList<>();
        for (LiteraryEraEntity era : eras.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new ReviewItem("ERA", era.getId(), era.getNameOriginal(), era.getStatus().name(), era.getVersion()));
        }
        for (LiteraryGenreEntity genre : genres.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new ReviewItem("GENRE", genre.getId(), genre.getNameOriginal(), genre.getStatus().name(), genre.getVersion()));
        }
        for (LiterarySchoolEntity school : schools.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new ReviewItem("SCHOOL", school.getId(), school.getNameOriginal(), school.getStatus().name(), school.getVersion()));
        }
        for (LiteraryFigureEntity figure : figures.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new ReviewItem("FIGURE", figure.getId(), figure.getCanonicalName(), figure.getStatus().name(), figure.getVersion()));
        }
        for (LiteraryWorkEntity work : works.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new ReviewItem("WORK", work.getId(), work.getTitleOriginal(), work.getStatus().name(), work.getVersion()));
        }
        return items;
    }

    private EraAdmin moveEra(UUID id, long version, String action) {
        LiteraryEraEntity era = lockedEra(id, version);
        UUID actor = authorization.requireAccess().userId();
        AuditEventType event = transition(era, actor, action);
        editorial.persist(eras, era, actor, event, ERA);
        return eraAdmin(era);
    }

    private GenreAdmin moveGenre(UUID id, long version, String action) {
        LiteraryGenreEntity genre = lockedGenre(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.persist(genres, genre, actor, transition(genre, actor, action), GENRE);
        return genreAdmin(genre);
    }

    private SchoolAdmin moveSchool(UUID id, long version, String action) {
        LiterarySchoolEntity school = lockedSchool(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.persist(schools, school, actor, transition(school, actor, action), SCHOOL);
        return schoolAdmin(school);
    }

    private FigureAdmin moveFigure(UUID id, long version, String action) {
        LiteraryFigureEntity figure = lockedFigure(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.persist(figures, figure, actor, transition(figure, actor, action), FIGURE);
        return figureAdmin(figure);
    }

    private WorkAdmin moveWork(UUID id, long version, String action) {
        LiteraryWorkEntity work = lockedWork(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.persist(works, work, actor, transition(work, actor, action), WORK);
        return workAdmin(work);
    }

    private AuditEventType transition(EditorialEntity record, UUID actor, String action) {
        if ("submit".equals(action)) {
            editorial.submit(record);
            return AuditEventType.LITERARY_CONTENT_SUBMITTED;
        }
        editorial.review(record, actor);
        return AuditEventType.LITERARY_CONTENT_VERIFIED;
    }

    private Map<String, Object> eraSnapshot(LiteraryEraEntity era, List<Map<String, Object>> sources) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("name", era.getNameOriginal());
        snapshot.put("slug", era.getSlug());
        snapshot.put("startDescription", era.getStartDescription());
        snapshot.put("endDescription", era.getEndDescription());
        snapshot.put("summary", era.getSummary());
        snapshot.put("historicalContext", era.getHistoricalContext());
        snapshot.put("sources", sources);
        return snapshot;
    }

    private Map<String, Object> genreSnapshot(LiteraryGenreEntity genre, List<Map<String, Object>> sources) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("name", genre.getNameOriginal());
        snapshot.put("slug", genre.getSlug());
        snapshot.put("description", genre.getDescription());
        snapshot.put("sources", sources);
        return snapshot;
    }

    private Map<String, Object> schoolSnapshot(LiterarySchoolEntity school, List<Map<String, Object>> sources) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("name", school.getNameOriginal());
        snapshot.put("slug", school.getSlug());
        snapshot.put("description", school.getDescription());
        snapshot.put("era", eraLink(school.getEraId()));
        snapshot.put("sources", sources);
        return snapshot;
    }

    private Map<String, Object> figureSnapshot(LiteraryFigureEntity figure, List<Map<String, Object>> sources) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("name", figure.getCanonicalName());
        snapshot.put("slug", figure.getSlug());
        snapshot.put("biography", figure.getBiographySummary());
        snapshot.put("birthLabel", birth(figure).publicLabel());
        snapshot.put("deathLabel", death(figure).publicLabel());
        List<Map<String, Object>> aliasMaps = new ArrayList<>();
        for (LiteraryFigureAliasEntity alias : aliases.findByFigureIdOrderByAliasAsc(figure.getId())) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("alias", alias.getAlias());
            map.put("kind", alias.getKind().name());
            aliasMaps.add(map);
        }
        snapshot.put("aliases", aliasMaps);
        snapshot.put("roles", roles.findByFigureIdOrderByRoleAsc(figure.getId()).stream().map(role -> role.getRole().name()).toList());
        List<Map<String, Object>> eraMaps = new ArrayList<>();
        for (LiteraryFigureEraEntity link : figureEras.findByFigureId(figure.getId())) {
            eras.findById(link.getEraId()).filter(LiteraryEraEntity::visibleToPublic).ifPresent(era -> {
                Map<String, Object> map = eraLink(era.getId());
                if (map != null) {
                    eraMaps.add(map);
                }
            });
        }
        snapshot.put("eras", eraMaps);
        List<Map<String, Object>> schoolMaps = new ArrayList<>();
        for (LiterarySchoolFigureEntity link : schoolFigures.findByFigureId(figure.getId())) {
            schools.findById(link.getSchoolId()).filter(LiterarySchoolEntity::visibleToPublic).ifPresent(school -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", labeledName(school.getNameOriginal(), school));
                map.put("slug", school.getSlug());
                schoolMaps.add(map);
            });
        }
        snapshot.put("schools", schoolMaps);
        List<Map<String, Object>> workMaps = new ArrayList<>();
        for (LiteraryWorkFigureEntity link : workFigures.findByFigureId(figure.getId())) {
            works.findById(link.getWorkId()).filter(LiteraryWorkEntity::visibleToPublic).ifPresent(work -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("title", labeledTitle(work));
                map.put("slug", work.getSlug());
                map.put("roleLabel", link.getRoleLabel());
                workMaps.add(map);
            });
        }
        snapshot.put("works", workMaps);
        snapshot.put("sources", sources);
        return snapshot;
    }

    private Map<String, Object> workSnapshot(LiteraryWorkEntity work, List<LiteraryExcerptEntity> samples, List<Map<String, Object>> sources) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("title", work.getTitleOriginal());
        snapshot.put("slug", work.getSlug());
        snapshot.put("description", work.getDescription());
        snapshot.put("languageCode", work.getLanguageCode());
        snapshot.put("genre", genreLink(work.getGenreId()));
        snapshot.put("era", eraLink(work.getEraId()));
        snapshot.put("compositionDisplay", work.getCompositionDisplay());
        snapshot.put("rights", work.getRightsStatus().name());
        snapshot.put("rightsNote", work.getRightsNote());
        snapshot.put("attribution", work.getAttribution());
        snapshot.put("aliases", workAliases.findByWorkIdOrderByAliasAsc(work.getId()).stream().map(LiteraryWorkAliasEntity::getAlias).toList());
        List<Map<String, Object>> figureMaps = new ArrayList<>();
        for (LiteraryWorkFigureEntity link : workFigures.findByWorkId(work.getId())) {
            figures.findById(link.getFigureId()).filter(LiteraryFigureEntity::visibleToPublic).ifPresent(figure -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", labeledFigure(figure));
                map.put("slug", figure.getSlug());
                map.put("roleLabel", link.getRoleLabel());
                figureMaps.add(map);
            });
        }
        snapshot.put("figures", figureMaps);
        if (work.getRightsStatus().allowsExcerpt()) {
            List<Map<String, Object>> excerptMaps = new ArrayList<>();
            for (LiteraryExcerptEntity excerpt : samples) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("text", excerpt.getExcerptText());
                excerptMaps.add(map);
            }
            snapshot.put("excerpts", excerptMaps);
        }
        snapshot.put("sources", sources);
        return snapshot;
    }

    private Map<String, Object> eraLink(UUID eraId) {
        if (eraId == null) {
            return null;
        }
        return eras.findById(eraId).filter(LiteraryEraEntity::visibleToPublic).map(era -> {
            Map<String, Object> link = new LinkedHashMap<>();
            link.put("name", labeledName(era.getNameOriginal(), era));
            link.put("slug", era.getSlug());
            return link;
        }).orElse(null);
    }

    private Map<String, Object> genreLink(UUID genreId) {
        if (genreId == null) {
            return null;
        }
        return genres.findById(genreId).filter(LiteraryGenreEntity::visibleToPublic).map(genre -> {
            Map<String, Object> link = new LinkedHashMap<>();
            link.put("name", labeledName(genre.getNameOriginal(), genre));
            link.put("slug", genre.getSlug());
            return link;
        }).orElse(null);
    }

    private static String labeledName(String live, EditorialEntity record) {
        if (record.visibleToPublic() && record.getPublishedSnapshot().get("name") != null) {
            String name = String.valueOf(record.getPublishedSnapshot().get("name")).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }
        return live;
    }

    private static String labeledTitle(LiteraryWorkEntity work) {
        if (work.visibleToPublic() && work.getPublishedSnapshot().get("title") != null) {
            String title = String.valueOf(work.getPublishedSnapshot().get("title")).trim();
            if (!title.isEmpty()) {
                return title;
            }
        }
        return work.getTitleOriginal();
    }

    private static String labeledFigure(LiteraryFigureEntity figure) {
        if (figure.visibleToPublic() && figure.getPublishedSnapshot().get("name") != null) {
            String name = String.valueOf(figure.getPublishedSnapshot().get("name")).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }
        return figure.getCanonicalName();
    }

    private void applyEra(LiteraryEraEntity era, String name, String start, String end, String summary, String context, Integer displayOrder) {
        String original = KnowledgeText.required(name, "name", 160);
        era.setNameOriginal(original);
        era.setNameNormalized(KnowledgeText.normalized(original));
        era.setStartDescription(KnowledgeText.optional(start, "startDescription", 300));
        era.setEndDescription(KnowledgeText.optional(end, "endDescription", 300));
        era.setSummary(KnowledgeText.optional(summary, "summary", 2000));
        era.setHistoricalContext(KnowledgeText.optional(context, "historicalContext", 4000));
        era.setDisplayOrder(order(displayOrder));
    }

    private void applyGenre(LiteraryGenreEntity genre, String name, String description) {
        String original = KnowledgeText.required(name, "name", 160);
        genre.setNameOriginal(original);
        genre.setNameNormalized(KnowledgeText.normalized(original));
        genre.setDescription(KnowledgeText.optional(description, "description", 2000));
    }

    private void applySchool(LiterarySchoolEntity school, String name, String description, UUID eraId) {
        String original = KnowledgeText.required(name, "name", 160);
        school.setNameOriginal(original);
        school.setNameNormalized(KnowledgeText.normalized(original));
        school.setDescription(KnowledgeText.optional(description, "description", 4000));
        school.setEraId(optionalEra(eraId));
    }

    private void applyFigure(LiteraryFigureEntity figure, String name, String biography, DateInput birth, DateInput death) {
        String original = KnowledgeText.required(name, "canonicalName", 160);
        figure.setCanonicalName(original);
        figure.setNormalizedName(KnowledgeText.normalized(original));
        figure.setBiographySummary(KnowledgeText.optional(biography, "biographySummary", 4000));
        HistoricalDate born = date(birth);
        HistoricalDate died = date(death);
        figure.setBirthPrecision(born.precision());
        figure.setBirthCalendar(born.calendar());
        figure.setBirthYear(born.year());
        figure.setBirthExactDate(born.exactDate());
        figure.setBirthDisplay(born.displayText());
        figure.setBirthCirca(born.circa());
        figure.setDeathPrecision(died.precision());
        figure.setDeathCalendar(died.calendar());
        figure.setDeathYear(died.year());
        figure.setDeathExactDate(died.exactDate());
        figure.setDeathDisplay(died.displayText());
        figure.setDeathCirca(died.circa());
    }

    private void applyWork(LiteraryWorkEntity work, String title, String description, String languageCode, UUID genreId, UUID eraId, String composition, String attribution) {
        String original = KnowledgeText.required(title, "title", 200);
        work.setTitleOriginal(original);
        work.setTitleNormalized(KnowledgeText.normalized(original));
        work.setDescription(KnowledgeText.optional(description, "description", 4000));
        work.setLanguageCode(language(languageCode));
        work.setGenreId(optionalGenre(genreId));
        work.setEraId(optionalEra(eraId));
        work.setCompositionDisplay(KnowledgeText.optional(composition, "compositionDisplay", 200));
        work.setAttribution(KnowledgeText.optional(attribution, "attribution", 500));
    }

    private UUID optionalEra(UUID eraId) {
        if (eraId == null) {
            return null;
        }
        requireEra(eraId);
        return eraId;
    }

    private UUID optionalGenre(UUID genreId) {
        if (genreId == null) {
            return null;
        }
        if (genres.findById(genreId).isEmpty()) {
            throw missing("Genre");
        }
        return genreId;
    }

    private void requireEra(UUID eraId) {
        if (eraId == null || eras.findById(eraId).isEmpty()) {
            throw missing("Era");
        }
    }

    private void requireSchool(UUID schoolId) {
        if (schoolId == null || schools.findById(schoolId).isEmpty()) {
            throw missing("School");
        }
    }

    private void requireFigure(UUID figureId) {
        if (figureId == null || figures.findById(figureId).isEmpty()) {
            throw missing("Figure");
        }
    }

    private LiteraryEraEntity lockedEra(UUID id, long version) {
        LiteraryEraEntity era = eras.lockById(id).orElseThrow(() -> missing("Era"));
        editorial.requireVersion(era, version);
        return era;
    }

    private LiteraryGenreEntity lockedGenre(UUID id, long version) {
        LiteraryGenreEntity genre = genres.lockById(id).orElseThrow(() -> missing("Genre"));
        editorial.requireVersion(genre, version);
        return genre;
    }

    private LiterarySchoolEntity lockedSchool(UUID id, long version) {
        LiterarySchoolEntity school = schools.lockById(id).orElseThrow(() -> missing("School"));
        editorial.requireVersion(school, version);
        return school;
    }

    private LiteraryFigureEntity lockedFigure(UUID id, long version) {
        LiteraryFigureEntity figure = figures.lockById(id).orElseThrow(() -> missing("Figure"));
        editorial.requireVersion(figure, version);
        return figure;
    }

    private LiteraryWorkEntity lockedWork(UUID id, long version) {
        LiteraryWorkEntity work = works.lockById(id).orElseThrow(() -> missing("Work"));
        editorial.requireVersion(work, version);
        return work;
    }

    private EraAdmin eraAdmin(LiteraryEraEntity era) {
        return new EraAdmin(era.getId(), era.getNameOriginal(), era.getSlug(), era.getStartDescription(), era.getEndDescription(), era.getSummary(), era.getHistoricalContext(), era.getDisplayOrder(), era.getStatus().name(), era.getVersion(), ids(eraCitations.findByOwnerId(era.getId())));
    }

    private GenreAdmin genreAdmin(LiteraryGenreEntity genre) {
        return new GenreAdmin(genre.getId(), genre.getNameOriginal(), genre.getSlug(), genre.getDescription(), genre.getStatus().name(), genre.getVersion(), ids(genreCitations.findByOwnerId(genre.getId())));
    }

    private SchoolAdmin schoolAdmin(LiterarySchoolEntity school) {
        return new SchoolAdmin(school.getId(), school.getEraId(), school.getNameOriginal(), school.getSlug(), school.getDescription(), school.getStatus().name(), school.getVersion(), ids(schoolCitations.findByOwnerId(school.getId())));
    }

    private FigureAdmin figureAdmin(LiteraryFigureEntity figure) {
        List<AliasView> aliasViews = aliases.findByFigureIdOrderByAliasAsc(figure.getId()).stream()
                .map(alias -> new AliasView(alias.getId(), alias.getAlias(), alias.getKind().name()))
                .toList();
        List<String> roleNames = roles.findByFigureIdOrderByRoleAsc(figure.getId()).stream().map(role -> role.getRole().name()).toList();
        List<EraLinkView> eraViews = new ArrayList<>();
        for (LiteraryFigureEraEntity link : figureEras.findByFigureId(figure.getId())) {
            eras.findById(link.getEraId()).ifPresent(era -> eraViews.add(new EraLinkView(era.getId(), era.getNameOriginal(), era.getSlug())));
        }
        List<SchoolLinkView> schoolViews = new ArrayList<>();
        for (LiterarySchoolFigureEntity link : schoolFigures.findByFigureId(figure.getId())) {
            schools.findById(link.getSchoolId()).ifPresent(school -> schoolViews.add(new SchoolLinkView(school.getId(), school.getNameOriginal(), school.getSlug())));
        }
        List<WorkLinkView> workViews = new ArrayList<>();
        for (LiteraryWorkFigureEntity link : workFigures.findByFigureId(figure.getId())) {
            works.findById(link.getWorkId()).ifPresent(work -> workViews.add(new WorkLinkView(work.getId(), work.getTitleOriginal(), work.getSlug(), link.getRoleLabel())));
        }
        return new FigureAdmin(figure.getId(), figure.getCanonicalName(), figure.getSlug(), figure.getBiographySummary(), dateView(birth(figure)), dateView(death(figure)), figure.getStatus().name(), figure.getVersion(), aliasViews, roleNames, eraViews, schoolViews, workViews, ids(figureCitations.findByOwnerId(figure.getId())));
    }

    private WorkAdmin workAdmin(LiteraryWorkEntity work) {
        List<String> aliasNames = workAliases.findByWorkIdOrderByAliasAsc(work.getId()).stream().map(LiteraryWorkAliasEntity::getAlias).toList();
        List<FigureLinkView> figureViews = new ArrayList<>();
        for (LiteraryWorkFigureEntity link : workFigures.findByWorkId(work.getId())) {
            figures.findById(link.getFigureId()).ifPresent(figure -> figureViews.add(new FigureLinkView(figure.getId(), figure.getCanonicalName(), figure.getSlug(), link.getRoleLabel())));
        }
        List<ExcerptView> excerptViews = excerpts.findByWorkIdOrderByDisplayOrderAsc(work.getId()).stream()
                .map(excerpt -> new ExcerptView(excerpt.getId(), excerpt.getExcerptText(), excerpt.getCitationId(), excerpt.getDisplayOrder()))
                .toList();
        return new WorkAdmin(work.getId(), work.getTitleOriginal(), work.getSlug(), work.getDescription(), work.getLanguageCode(), work.getGenreId(), work.getEraId(), work.getCompositionDisplay(), work.getRightsStatus().name(), work.getRightsNote(), work.getAttribution(), work.getStatus().name(), work.getVersion(), aliasNames, figureViews, excerptViews, ids(workCitations.findByOwnerId(work.getId())));
    }

    private static HistoricalDate birth(LiteraryFigureEntity figure) {
        return new HistoricalDate(figure.getBirthPrecision(), figure.getBirthCalendar(), figure.getBirthYear(), figure.getBirthExactDate(), figure.getBirthDisplay(), figure.isBirthCirca());
    }

    private static HistoricalDate death(LiteraryFigureEntity figure) {
        return new HistoricalDate(figure.getDeathPrecision(), figure.getDeathCalendar(), figure.getDeathYear(), figure.getDeathExactDate(), figure.getDeathDisplay(), figure.isDeathCirca());
    }

    private static DateView dateView(HistoricalDate date) {
        return new DateView(date.precision().name(), date.calendar().name(), date.year(), date.exactDate(), date.displayText(), date.circa(), date.publicLabel());
    }

    private static HistoricalDate date(DateInput input) {
        if (input == null) {
            return HistoricalDate.unknown();
        }
        return new HistoricalDate(input.precision(), input.calendar(), input.year(), input.exactDate(), input.displayText(), Boolean.TRUE.equals(input.circa()));
    }

    private List<Map<String, Object>> sourcesOf(Collection<UUID> citationIds) {
        return SourceLines.of(citations.requirePublishable(citationIds));
    }

    private static List<UUID> ids(List<? extends OwnerCitationEntity> links) {
        return links.stream().map(OwnerCitationEntity::getCitationId).toList();
    }

    private static String language(String code) {
        String value = KnowledgeText.plain(code, "languageCode", 16);
        return value == null ? "ar" : value;
    }

    private static int order(Integer displayOrder) {
        if (displayOrder == null) {
            return 0;
        }
        if (displayOrder < 0) {
            throw invalid("displayOrder", "Display order cannot be negative.");
        }
        return displayOrder;
    }

    private static Pageable page(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw invalid("size", "Page size must be from 1 to 50.");
        }
        return PageRequest.of(page, size);
    }

    private static ResourceNotFoundException missing(String name) {
        return new ResourceNotFoundException(name + " was not found.");
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
