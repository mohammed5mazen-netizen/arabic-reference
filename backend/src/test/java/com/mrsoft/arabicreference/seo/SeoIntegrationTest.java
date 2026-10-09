package com.mrsoft.arabicreference.seo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mrsoft.arabicreference.IntegrationContainers;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SeoIntegrationTest {

    static final String OWNER_PASSWORD = "Owner-Pass-123!";

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.rate-limit.login", () -> "1000");
        registry.add("app.admin.rate-limit.refresh", () -> "1000");
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> OWNER_PASSWORD);
        registry.add("app.seo.site-url", () -> "https://reference.example");
        registry.add("app.seo.indexing-enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void publishedKnowledgeIsDiscoverableAndPrivatePathsStayOut() throws Exception {
        mockMvc.perform(get("/api/v1/admin/seo/status")).andExpect(status().isUnauthorized());
        String editor = member("s12e", UUID.fromString("a0000000-0000-4000-8000-000000000003"));
        String reviewer = member("s12r", UUID.fromString("a0000000-0000-4000-8000-000000000004"));
        String publisher = member("s12p", UUID.fromString("a0000000-0000-4000-8000-000000000005"));
        String auditor = member("s12a", UUID.fromString("a0000000-0000-4000-8000-000000000006"));
        mockMvc.perform(get("/api/v1/admin/seo/status").header("Authorization", bearer(editor))).andExpect(status().isForbidden());

        String draft = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lemma\":\"فهرسة\",\"vocalizedForm\":null,\"rootId\":null,\"partOfSpeech\":\"NOUN\",\"gender\":\"MASCULINE\"}"))
                .andExpect(status().isOk()).andReturn());
        mockMvc.perform(get("/api/v1/public/dictionary/by-slug/" + slug(draft))).andExpect(status().isNotFound());
        assertThat(paths()).doesNotContain("/word/" + slug(draft));

        String source = publishSource(editor, reviewer, publisher, "مقاييس الفهرسة");
        String citation = body(mockMvc.perform(post("/api/v1/admin/sources/" + id(source) + "/citations")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageFrom\":1,\"pageTo\":1,\"entryLabel\":\"فهرسة\"}"))
                .andExpect(status().isOk()).andReturn());
        draft = addSense(editor, draft);
        draft = linkSense(editor, draft, citation);
        draft = act(editor, "/api/v1/admin/dictionary/entries/" + id(draft) + "/submit", draft);
        draft = act(reviewer, "/api/v1/admin/dictionary/entries/" + id(draft) + "/verify", draft);
        mockMvc.perform(post("/api/v1/admin/editorial/quality/scans")
                        .header("Authorization", bearer(publisher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scope\":\"RECORD\",\"contentType\":\"DICTIONARY_ENTRY\",\"contentId\":\"" + id(draft) + "\"}"))
                .andExpect(status().isOk());
        String published = act(publisher, "/api/v1/admin/dictionary/entries/" + id(draft) + "/publish", draft);
        String path = "/word/" + slug(published);
        assertThat(paths()).contains(path);
        mockMvc.perform(get("/api/v1/public/discovery").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].title").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].entityId").doesNotExist());

        UUID poisoned = UUID.randomUUID();
        jdbc.update("""
                insert into search_document (
                    id, entity_type, entity_id, title_original, title_normalized, search_key, searchable_text,
                    snippet, url_path, published_at, related_count, popularity, source_quality, index_version)
                values (?, 'ARTICLE', ?, 'سري', 'سري', 'secret', 'secret', 'secret', '/admin/secret?q=1', now(), 0, 0, 0, 1)
                """, poisoned, poisoned);
        assertThat(paths()).doesNotContain("/admin/secret?q=1");
        jdbc.update("delete from search_document where id = ?", poisoned);

        String archived = act(publisher, "/api/v1/admin/dictionary/entries/" + id(published) + "/archive", published);
        assertThat(slug(archived)).isEqualTo(slug(published));
        assertThat(paths()).doesNotContain(path);
        mockMvc.perform(get("/api/v1/public/dictionary/by-slug/" + slug(published))).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/public/dictionary/by-slug/missing-word")).andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/admin/seo/status").header("Authorization", bearer(auditor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteOrigin").value("https://reference.example"))
                .andExpect(jsonPath("$.data.indexingEnabled").value(false))
                .andExpect(jsonPath("$.data.indexableCount").isNumber())
                .andExpect(jsonPath("$.data.password").doesNotExist());
        mockMvc.perform(get("/api/v1/public/learning/references").param("kind", "DICTIONARY_ENTRY").param("slug", slug(published)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    private java.util.List<String> paths() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/public/discovery").param("page", "0").param("size", "500")).andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.items[*].path");
    }

    private String publishSource(String editor, String reviewer, String publisher, String title) throws Exception {
        String source = body(mockMvc.perform(post("/api/v1/admin/sources")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceType\":\"BOOK\",\"title\":\"" + title + "\",\"author\":\"الخليل\",\"edition\":\"1\",\"licenseType\":\"CC_BY\",\"publicDomain\":false,\"attributionText\":\"نسب\"}"))
                .andExpect(status().isOk()).andReturn());
        source = act(editor, "/api/v1/admin/sources/" + id(source) + "/submit", source);
        source = act(reviewer, "/api/v1/admin/sources/" + id(source) + "/verify", source);
        return act(publisher, "/api/v1/admin/sources/" + id(source) + "/publish", source);
    }

    private String addSense(String token, String entry) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/senses")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + version(entry) + ",\"sense\":{\"definition\":\"ترتيب المعرفة المنشورة ليسهل العثور عليها\",\"shortDefinition\":\"ترتيب للعثور\",\"usageLabel\":\"CLASSICAL\",\"domainLabel\":\"LANGUAGE\",\"displayOrder\":1}}"))
                .andExpect(status().isOk()).andReturn());
    }

    private String linkSense(String token, String entry, String citation) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/senses/" + senseId(entry) + "/citations")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + version(entry) + ",\"citationId\":\"" + id(citation) + "\"}"))
                .andExpect(status().isOk()).andReturn());
    }

    private String member(String username, UUID role) throws Exception {
        String owner = token("owner", OWNER_PASSWORD);
        MvcResult created = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"email\":\"" + username + "@arabic-reference.test\",\"displayName\":\"" + username + "\",\"roleIds\":[\"" + role + "\"]}"))
                .andExpect(status().isOk()).andReturn();
        String temporary = JsonPath.read(created.getResponse().getContentAsString(), "$.data.temporaryPassword");
        String current = token(username, temporary);
        MvcResult changed = mockMvc.perform(post("/api/v1/admin/auth/change-password")
                        .header("Authorization", bearer(current))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + temporary + "\",\"newPassword\":\"Staff-Pass-123!\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(changed.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private String token(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private String act(String token, String path, String resource) throws Exception {
        return body(mockMvc.perform(post(path)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + version(resource) + "}"))
                .andExpect(status().isOk()).andReturn());
    }

    private static String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString();
    }

    private static String id(String payload) {
        return JsonPath.read(payload, "$.data.id");
    }

    private static String slug(String payload) {
        return JsonPath.read(payload, "$.data.slug");
    }

    private static String senseId(String payload) {
        return JsonPath.read(payload, "$.data.senses[0].id");
    }

    private static long version(String payload) {
        Number value = JsonPath.read(payload, "$.data.version");
        return value.longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
