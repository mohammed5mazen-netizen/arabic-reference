package com.mrsoft.arabicreference.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mrsoft.arabicreference.IntegrationContainers;
import com.mrsoft.arabicreference.identity.application.AdminSessionService;
import com.mrsoft.arabicreference.identity.application.AdminUserService;
import com.mrsoft.arabicreference.identity.application.OwnerBootstrap;
import com.mrsoft.arabicreference.identity.domain.AccountStatus;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.identity.infrastructure.security.JwtAccessTokens;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.UnauthorizedException;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AdminIdentityIntegrationTest {

    static final UUID ADMIN_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000002");
    static final UUID EDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000003");
    static final UUID PUBLISHER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000005");
    static final UUID AUDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    static final String OWNER_PASSWORD = "Owner-Pass-123!";

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.max-failed-attempts", () -> "3");
        registry.add("app.admin.rate-limit.login", () -> "1000");
        registry.add("app.admin.rate-limit.refresh", () -> "1000");
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> OWNER_PASSWORD);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OwnerBootstrap bootstrap;

    @Autowired
    private AdminUserService users;

    @Autowired
    private AdminSessionService sessions;

    @Autowired
    private JwtAccessTokens accessTokens;

    @Test
    void flywayAppliesAdminIdentityWithoutChangingTheFoundationMarker() {
        Integer version1 = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = true and version = '1'", Integer.class);
        Integer version2 = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = true and version = '2'", Integer.class);
        Integer permissions = jdbc.queryForObject("select count(*) from admin_permission", Integer.class);
        assertThat(version1).isEqualTo(1);
        assertThat(version2).isEqualTo(1);
        assertThat(permissions).isEqualTo(PermissionCatalog.all().size());
        assertThat(jdbc.queryForList("select code from admin_permission", String.class))
                .containsExactlyInAnyOrderElementsOf(PermissionCatalog.all());
    }

    @Test
    void bootstrapIsIdempotentAndDoesNotStoreThePassword() {
        String before = passwordHash("owner");
        assertThat(before).doesNotContain(OWNER_PASSWORD);
        assertThat(passwordEncoder.matches(OWNER_PASSWORD, before)).isTrue();
        bootstrap.ensure();
        assertThat(passwordHash("owner")).isEqualTo(before);
        assertThat(jdbc.queryForObject("select count(*) from admin_user where username = 'owner'", Integer.class)).isEqualTo(1);
    }

    @Test
    void publicReadsStayAnonymousAndAdminRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(get("/api/v1/public/foundation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access").value("public-anonymous"));
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.redis.status").value("UP"));
    }

    @Test
    void ownerCanManageStaffAndAMissingPermissionIsForbidden() throws Exception {
        Tokens owner = login("owner", OWNER_PASSWORD);
        assertThat(owner.body()).doesNotContain(OWNER_PASSWORD).doesNotContain("argon2");

        MvcResult created = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin.one","email":"Admin.One@Arabic-Reference.test","displayName":"مدير النظام","roleIds":["%s"]}
                                """.formatted(ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.username").value("admin.one"))
                .andExpect(jsonPath("$.data.user.email").value("admin.one@arabic-reference.test"))
                .andExpect(jsonPath("$.data.user.displayName").value("مدير النظام"))
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.temporaryPassword").isNotEmpty())
                .andReturn();
        String temporary = JsonPath.read(created.getResponse().getContentAsString(), "$.data.temporaryPassword");
        String adminId = JsonPath.read(created.getResponse().getContentAsString(), "$.data.user.id");

        mockMvc.perform(get("/api/v1/admin/users/" + adminId).header("Authorization", bearer(owner.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.temporaryPassword").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        Tokens limited = login("admin.one", temporary);
        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(limited.accessToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));

        Tokens admin = changePassword(limited, temporary, "Admin-Pass-123!");
        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(admin.accessToken())))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/admin/users/" + adminId + "/roles")
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleIds":["%s"],"version":%s}
                                """.formatted(PUBLISHER_ROLE, versionOf(admin.accessToken(), adminId))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_OPERATION"));

        mockMvc.perform(post("/api/v1/admin/roles")
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"ESCALATED","name":"تصعيد","description":"Should fail","permissionCodes":["editorial.content.publish"]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshRotationLogoutReuseAndExpiredTokens() throws Exception {
        Tokens owner = login("OWNER", OWNER_PASSWORD);
        Tokens rotated = refresh(owner.refreshToken());
        mockMvc.perform(post("/api/v1/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(owner.refreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_REUSED"));
        mockMvc.perform(post("/api/v1/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(rotated.refreshToken())))
                .andExpect(status().isUnauthorized());

        Tokens again = login("owner", OWNER_PASSWORD);
        mockMvc.perform(post("/api/v1/admin/auth/logout").header("Authorization", bearer(again.accessToken())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/auth/session").header("Authorization", bearer(again.accessToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));

        UUID ownerId = userId("owner");
        var expired = accessTokens.issue(ownerId, Instant.now().minus(Duration.ofMinutes(5)), Instant.now().minus(Duration.ofMinutes(2)));
        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(expired.value())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_EXPIRED"));
    }

    @Test
    void lockedAndDisabledAccountsCannotKeepUsingAccessTokens() throws Exception {
        Tokens owner = login("owner", OWNER_PASSWORD);
        String userId = createUser(owner, "lock.user", "lock.user@arabic-reference.test", "حساب مقفل", EDITOR_ROLE);
        String temporary = temporaryPassword(owner, userId);
        Tokens first = login("lock.user", temporary);
        Tokens active = changePassword(first, temporary, "Locked-Pass-123!");
        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"lock.user","password":"Wrong-Pass-123!"}
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
        assertThat(statusOf("lock.user")).isEqualTo(AccountStatus.LOCKED.name());
        mockMvc.perform(get("/api/v1/admin/auth/session").header("Authorization", bearer(active.accessToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"lock.user","password":"Locked-Pass-123!"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));

        long version = versionOf(owner.accessToken(), userId);
        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/unlock")
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d}
                                """.formatted(version)))
                .andExpect(status().isOk());
        Tokens restored = login("lock.user", "Locked-Pass-123!");
        version = versionOf(owner.accessToken(), userId);
        mockMvc.perform(post("/api/v1/admin/users/" + userId + "/deactivate")
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d}
                                """.formatted(version)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/auth/session").header("Authorization", bearer(restored.accessToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"));
    }

    @Test
    void lastOwnerCannotBeDeactivatedOrStrippedAndFailedLoginsDoNotLockThatOwner() throws Exception {
        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"missing.owner","password":"Wrong-Pass-123!"}
                                    """))
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"owner","password":"Wrong-Pass-123!"}
                                    """))
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
        assertThat(statusOf("owner")).isEqualTo(AccountStatus.ACTIVE.name());
        login("owner", OWNER_PASSWORD);

        UUID ownerId = userId("owner");
        long version = jdbc.queryForObject("select version from admin_user where id = ?", Long.class, ownerId);
        asOwner(ownerId);
        SecurityContext ownerContext = SecurityContextHolder.getContext();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?> first = executor.submit(() -> deactivateAsOwner(ownerContext, ownerId, version, start));
            Future<?> second = executor.submit(() -> deactivateAsOwner(ownerContext, ownerId, version, start));
            start.countDown();
            assertThatThrownBy(() -> first.get(30, TimeUnit.SECONDS)).hasCauseInstanceOf(ForbiddenOperationException.class);
            assertThatThrownBy(() -> second.get(30, TimeUnit.SECONDS)).hasCauseInstanceOf(ForbiddenOperationException.class);
        } finally {
            executor.shutdownNow();
            SecurityContextHolder.clearContext();
        }
        assertThat(statusOf("owner")).isEqualTo(AccountStatus.ACTIVE.name());

        Tokens owner = login("owner", OWNER_PASSWORD);
        long currentVersion = versionOf(owner.accessToken(), ownerId.toString());
        mockMvc.perform(put("/api/v1/admin/users/" + ownerId + "/roles")
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleIds":["%s"],"version":%d}
                                """.formatted(AUDITOR_ROLE, currentVersion)))
                .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("""
                select count(*) from admin_user_role links
                join admin_role roles on roles.id = links.role_id
                where links.user_id = ? and roles.code = 'PLATFORM_OWNER'
                """, Integer.class, ownerId)).isEqualTo(1);
    }

    @Test
    void staleUpdateConflictsAndRoleAssignmentIsPermissionAware() throws Exception {
        Tokens owner = login("owner", OWNER_PASSWORD);
        String editorId = createUser(owner, "editor.one", "editor.one@arabic-reference.test", "محرر", EDITOR_ROLE);
        long version = versionOf(owner.accessToken(), editorId);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/admin/users/" + editorId)
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"محرر محدّث","version":%d}
                                """.formatted(version)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("محرر محدّث"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/admin/users/" + editorId)
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"كتابة صامتة","version":%d}
                                """.formatted(version)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        UUID editorUuid = UUID.fromString(editorId);
        long current = jdbc.queryForObject("select version from admin_user where id = ?", Long.class, editorUuid);
        asOwner(userId("owner"));
        SecurityContext ownerContext = SecurityContextHolder.getContext();
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Runnable update = () -> {
                SecurityContextHolder.setContext(ownerContext);
                try {
                    start.await();
                    users.update(editorUuid, null, "تحديث متزامن", current);
                    successes.incrementAndGet();
                } catch (ConflictException exception) {
                    conflicts.incrementAndGet();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            };
            Future<?> first = executor.submit(update);
            Future<?> second = executor.submit(update);
            start.countDown();
            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
            SecurityContextHolder.clearContext();
        }
        assertThat(successes.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(1);

        version = versionOf(owner.accessToken(), editorId);
        mockMvc.perform(put("/api/v1/admin/users/" + editorId + "/roles")
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"roleIds":["%s","%s","%s"],"version":%d}
                                """.formatted(EDITOR_ROLE, EDITOR_ROLE, AUDITOR_ROLE, version)))
                .andExpect(status().isOk());
        Integer assignments = jdbc.queryForObject(
                "select count(*) from admin_user_role where user_id = ? and role_id = ?",
                Integer.class,
                editorUuid,
                EDITOR_ROLE);
        assertThat(assignments).isEqualTo(1);
        String temporary = temporaryPassword(owner, editorId);
        Tokens editor = changePassword(login("editor.one", temporary), temporary, "Editor-Pass-123!");
        mockMvc.perform(get("/api/v1/admin/audit").header("Authorization", bearer(editor.accessToken())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(editor.accessToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_OPERATION"));
    }

    @Test
    void auditIsAppendOnlyAndDoesNotStoreSecrets() throws Exception {
        login("owner", OWNER_PASSWORD);
        String metadata = jdbc.queryForObject("select coalesce(string_agg(metadata::text, ' '), '') from admin_audit_event", String.class);
        assertThat(metadata).doesNotContain(OWNER_PASSWORD).doesNotContain("argon2");
        assertThatThrownBy(() -> jdbc.update("delete from admin_audit_event"))
                .isInstanceOf(DataAccessException.class);
        Tokens owner = login("owner", OWNER_PASSWORD);
        mockMvc.perform(get("/api/v1/admin/audit").header("Authorization", bearer(owner.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
        mockMvc.perform(post("/api/v1/admin/audit").header("Authorization", bearer(owner.accessToken())))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
        mockMvc.perform(get("/api/v1/admin/dashboard").header("Authorization", bearer(owner.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.users.total").isNumber())
                .andExpect(jsonPath("$.data.recentAudit").isArray());
    }

    @Test
    void refreshRotationRaceRevokesTheFamily() throws Exception {
        Tokens owner = login("owner", OWNER_PASSWORD);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger reused = new AtomicInteger();
        try {
            Runnable refreshOnce = () -> {
                try {
                    start.await();
                    sessions.refresh(owner.refreshToken(), "203.0.113.10");
                    success.incrementAndGet();
                } catch (UnauthorizedException exception) {
                    reused.incrementAndGet();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            };
            Future<?> first = executor.submit(refreshOnce);
            Future<?> second = executor.submit(refreshOnce);
            start.countDown();
            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
        assertThat(success.get()).isEqualTo(1);
        assertThat(reused.get()).isEqualTo(1);
        assertThatThrownBy(() -> sessions.refresh(owner.refreshToken(), "203.0.113.11"))
                .isInstanceOf(UnauthorizedException.class);
    }

    private void deactivateAsOwner(SecurityContext context, UUID ownerId, long version, CountDownLatch start) {
        SecurityContextHolder.setContext(context);
        try {
            start.await();
            users.deactivate(ownerId, version);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private void asOwner(UUID ownerId) {
        var access = new AuthenticatedAccess(ownerId, UUID.randomUUID().toString(), false, Set.copyOf(PermissionCatalog.all()));
        var authentication = UsernamePasswordAuthenticationToken.authenticated(access, "", List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Tokens login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return tokens(result);
    }

    private Tokens refresh(String refreshToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andReturn();
        return tokens(result);
    }

    private Tokens changePassword(Tokens current, String currentPassword, String newPassword) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/change-password")
                        .header("Authorization", bearer(current.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"%s","newPassword":"%s"}
                                """.formatted(currentPassword, newPassword)))
                .andExpect(status().isOk())
                .andReturn();
        return tokens(result);
    }

    private String createUser(Tokens owner, String username, String email, String displayName, UUID roleId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","displayName":"%s","roleIds":["%s"]}
                                """.formatted(username, email, displayName, roleId)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.user.id");
    }

    private String temporaryPassword(Tokens owner, String userId) throws Exception {
        long version = versionOf(owner.accessToken(), userId);
        MvcResult result = mockMvc.perform(post("/api/v1/admin/users/" + userId + "/reset-password")
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d}
                                """.formatted(version)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.temporaryPassword");
    }

    private long versionOf(String accessToken, String userId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/users/" + userId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andReturn();
        Number version = JsonPath.read(result.getResponse().getContentAsString(), "$.data.version");
        return version.longValue();
    }

    private static Tokens tokens(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return new Tokens(body, JsonPath.read(body, "$.data.accessToken"), JsonPath.read(body, "$.data.refreshToken"));
    }

    private String passwordHash(String username) {
        return jdbc.queryForObject("select password_hash from admin_user where username = ?", String.class, username);
    }

    private String statusOf(String username) {
        return jdbc.queryForObject("select status from admin_user where username = ?", String.class, username);
    }

    private UUID userId(String username) {
        return jdbc.queryForObject("select id from admin_user where username = ?", UUID.class, username);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private record Tokens(String body, String accessToken, String refreshToken) {
    }
}
