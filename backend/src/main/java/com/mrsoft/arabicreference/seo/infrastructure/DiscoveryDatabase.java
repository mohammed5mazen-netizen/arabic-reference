package com.mrsoft.arabicreference.seo.infrastructure;

import com.mrsoft.arabicreference.seo.domain.PublicLocation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DiscoveryDatabase {

    private static final String PUBLIC_PATH = """
            url_path like '/%'
            and url_path not like '%?%'
            and url_path not like '%#%'
            and position('%' in url_path) = 0
            and url_path not like '/admin%'
            and url_path not like '/api%'
            and url_path not like '/search%'
            and url_path not like '%/attempts%'
            """;

    private final JdbcTemplate jdbc;

    public DiscoveryDatabase(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int count() {
        Integer total = jdbc.queryForObject("select count(*) from search_document where " + PUBLIC_PATH, Integer.class);
        return total == null ? 0 : total;
    }

    public List<PublicLocation> page(int limit, int offset) {
        return jdbc.query("""
                select entity_type, url_path, published_at
                from search_document
                where %s
                order by entity_type, url_path
                limit ? offset ?
                """.formatted(PUBLIC_PATH), (row, index) -> location(row), limit, offset);
    }

    public Map<String, Integer> countsByType() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        jdbc.query("""
                select entity_type, count(*) as total
                from search_document
                where %s
                group by entity_type
                order by entity_type
                """.formatted(PUBLIC_PATH), (row, index) -> counts.put(row.getString("entity_type"), row.getInt("total")));
        return counts;
    }

    public int invalidPaths() {
        Integer total = jdbc.queryForObject("""
                select count(*) from search_document
                where url_path like '%?%' or url_path like '%#%' or position('%' in url_path) > 0
                   or url_path like '/admin%' or url_path like '/api%' or url_path not like '/%'
                """, Integer.class);
        return total == null ? 0 : total;
    }

    public int unlinkedPublishedEntries() {
        Integer total = jdbc.queryForObject("""
                select count(*) from lexical_entry entry
                where entry.status = 'PUBLISHED' and entry.published_snapshot is not null and entry.root_id is null
                  and not exists (
                    select 1 from linguistic_relation relation
                    where relation.source_entry_id = entry.id or relation.target_entry_id = entry.id)
                """, Integer.class);
        return total == null ? 0 : total;
    }

    public int brokenPublishedRelations() {
        Integer total = jdbc.queryForObject("""
                select count(*) from linguistic_relation relation
                join lexical_entry source on source.id = relation.source_entry_id
                where source.status = 'PUBLISHED' and (
                    not exists (select 1 from lexical_entry target where target.id = relation.target_entry_id)
                    or exists (
                      select 1 from lexical_entry target
                      where target.id = relation.target_entry_id and target.status <> 'PUBLISHED'))
                """, Integer.class);
        return total == null ? 0 : total;
    }

    private static PublicLocation location(ResultSet row) throws SQLException {
        Timestamp published = row.getTimestamp("published_at");
        Instant instant = published == null ? null : published.toInstant();
        return new PublicLocation(row.getString("entity_type"), row.getString("url_path"), instant);
    }
}
