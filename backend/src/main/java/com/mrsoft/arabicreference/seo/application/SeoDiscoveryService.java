package com.mrsoft.arabicreference.seo.application;

import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.seo.domain.PublicLocation;
import com.mrsoft.arabicreference.seo.domain.SiteOrigin;
import com.mrsoft.arabicreference.seo.infrastructure.DiscoveryDatabase;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeoDiscoveryService {

    private static final int MAX_PAGE_SIZE = 500;

    private final DiscoveryDatabase database;
    private final AuthorizationService authorization;
    private final String configuredOrigin;
    private final boolean indexingEnabled;
    private final AtomicReference<Instant> lastGenerated = new AtomicReference<>();

    public SeoDiscoveryService(
            DiscoveryDatabase database,
            AuthorizationService authorization,
            @Value("${app.seo.site-url:}") String configuredOrigin,
            @Value("${app.seo.indexing-enabled:false}") boolean indexingEnabled) {
        this.database = database;
        this.authorization = authorization;
        this.configuredOrigin = configuredOrigin;
        this.indexingEnabled = indexingEnabled;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> locations(int page, int size) {
        if (page < 0 || page > 200) {
            throw new ValidationException("Page is out of range.", List.of(new FieldErrorDetail("page", "رقم الصفحة خارج النطاق.")));
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ValidationException("Page size is out of range.", List.of(new FieldErrorDetail("size", "حجم الصفحة بين 1 و 500.")));
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (PublicLocation location : database.page(size, page * size)) {
            if (!location.publishable()) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", location.type());
            item.put("path", location.path());
            if (location.publishedAt() != null) {
                item.put("publishedAt", location.publishedAt().toString());
            }
            items.add(item);
        }
        lastGenerated.set(Instant.now());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("page", page);
        body.put("size", size);
        body.put("total", database.count());
        return body;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> status() {
        authorization.requireAccess();
        if (!authorization.has(PermissionCatalog.SEO_ADMIN_VIEW)) {
            throw new ForbiddenOperationException("You do not have permission to perform this operation.");
        }
        SiteOrigin origin = SiteOrigin.parse(configuredOrigin).orElse(null);
        int invalidPaths = database.invalidPaths();
        int orphans = database.unlinkedPublishedEntries();
        int broken = database.brokenPublishedRelations();
        List<String> problems = new ArrayList<>();
        if (origin == null) {
            problems.add(configuredOrigin == null || configuredOrigin.isBlank() ? "SITE_URL is not configured." : "SITE_URL is invalid.");
        } else if (indexingEnabled && !origin.indexable()) {
            problems.add("Indexing is enabled for an origin that is not a public https site.");
        }
        if (invalidPaths > 0) {
            problems.add("Search documents contain paths that must stay out of the sitemap.");
        }
        if (broken > 0) {
            problems.add("Published dictionary relations point at missing or unpublished entries.");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("indexingEnabled", indexingEnabled);
        body.put("siteOrigin", origin == null ? "" : origin.value());
        body.put("counts", database.countsByType());
        body.put("indexableCount", database.count());
        Instant generated = lastGenerated.get();
        body.put("lastGenerated", generated == null ? "" : generated.toString());
        body.put("orphanEntries", orphans);
        body.put("brokenRelations", broken);
        body.put("invalidPaths", invalidPaths);
        body.put("problems", problems);
        return body;
    }
}
