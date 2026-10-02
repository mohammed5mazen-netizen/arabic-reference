package com.mrsoft.arabicreference.identity.application;

import com.mrsoft.arabicreference.identity.application.StaffViews.SessionView;
import com.mrsoft.arabicreference.identity.application.StaffViews.TokenPair;
import com.mrsoft.arabicreference.identity.domain.AccountStatus;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PasswordPolicy;
import com.mrsoft.arabicreference.identity.domain.TokenSecrets;
import com.mrsoft.arabicreference.identity.domain.Usernames;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminRefreshTokenEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminRefreshTokenRepository;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRepository;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRoleRepository;
import com.mrsoft.arabicreference.identity.infrastructure.security.JwtAccessTokens;
import com.mrsoft.arabicreference.identity.infrastructure.security.RedisAccessTokenDenylist;
import com.mrsoft.arabicreference.identity.infrastructure.security.RedisAuthRateLimiter;
import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.UnauthorizedException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSessionService {

    private static final String INVALID_CREDENTIALS = "The username or password is incorrect.";

    private final AdminUserRepository users;
    private final AdminUserRoleRepository links;
    private final AdminRefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtAccessTokens accessTokens;
    private final RedisAuthRateLimiter rateLimiter;
    private final RedisAccessTokenDenylist denylist;
    private final AuditRecorder audit;
    private final TimeProvider timeProvider;
    private final AdminSecurityProperties properties;
    private final AuthorizationService authorization;
    private final ObjectProvider<AdminSessionService> self;
    private final String dummyHash;

    public AdminSessionService(
            AdminUserRepository users,
            AdminUserRoleRepository links,
            AdminRefreshTokenRepository refreshTokens,
            PasswordEncoder passwordEncoder,
            JwtAccessTokens accessTokens,
            RedisAuthRateLimiter rateLimiter,
            RedisAccessTokenDenylist denylist,
            AuditRecorder audit,
            TimeProvider timeProvider,
            AdminSecurityProperties properties,
            AuthorizationService authorization,
            ObjectProvider<AdminSessionService> self) {
        this.users = users;
        this.links = links;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.accessTokens = accessTokens;
        this.rateLimiter = rateLimiter;
        this.denylist = denylist;
        this.audit = audit;
        this.timeProvider = timeProvider;
        this.properties = properties;
        this.authorization = authorization;
        this.self = self;
        this.dummyHash = passwordEncoder.encode(Ids.random().toString());
    }

    public TokenPair login(String rawUsername, String password, String clientAddress) {
        rateLimiter.checkLogin(clientAddress);
        String username = normalizeUsername(rawUsername);
        AdminUserEntity user = users.findByUsername(username).orElse(null);
        if (user == null) {
            passwordEncoder.matches(password, dummyHash);
            audit.record(null, AuditEventType.ADMIN_LOGIN_FAILED, "admin_user", null, Map.of("username", username));
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, INVALID_CREDENTIALS);
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            self.getObject().recordFailedAttempt(user.getId());
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, INVALID_CREDENTIALS);
        }
        if (user.getStatus() == AccountStatus.DISABLED) {
            audit.record(user.getId(), AuditEventType.ADMIN_LOGIN_FAILED, "admin_user", user.getId().toString(), Map.of("username", username, "reason", "disabled"));
            throw new UnauthorizedException(ErrorCode.ACCOUNT_DISABLED, "The account is disabled.");
        }
        Instant now = timeProvider.now();
        if (user.getStatus() == AccountStatus.LOCKED && (user.getLockedUntil() == null || user.getLockedUntil().isAfter(now))) {
            audit.record(user.getId(), AuditEventType.ADMIN_LOGIN_FAILED, "admin_user", user.getId().toString(), Map.of("username", username, "reason", "locked"));
            throw new UnauthorizedException(ErrorCode.ACCOUNT_LOCKED, "The account is temporarily locked.");
        }
        return self.getObject().completeLogin(user.getId());
    }

    @Transactional
    public void recordFailedAttempt(UUID userId) {
        AdminUserEntity user = users.lockById(userId).orElseThrow();
        boolean lastActiveOwner = links.countRole(user.getId(), com.mrsoft.arabicreference.identity.domain.RoleCodes.PLATFORM_OWNER) > 0
                && user.getStatus() == AccountStatus.ACTIVE
                && users.countOtherActiveOwners(user.getId()) == 0;
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (!lastActiveOwner && attempts >= properties.getMaxFailedAttempts()) {
            user.setStatus(AccountStatus.LOCKED);
            user.setLockedUntil(timeProvider.now().plus(properties.getLockoutDuration()));
        }
        user.setUpdatedAt(timeProvider.now());
        users.saveAndFlush(user);
        audit.record(null, AuditEventType.ADMIN_LOGIN_FAILED, "admin_user", user.getId().toString(), Map.of("username", user.getUsername()));
    }

    @Transactional
    public TokenPair completeLogin(UUID userId) {
        AdminUserEntity user = users.lockById(userId).orElseThrow();
        Instant now = timeProvider.now();
        if (user.getStatus() == AccountStatus.DISABLED) {
            throw new UnauthorizedException(ErrorCode.ACCOUNT_DISABLED, "The account is disabled.");
        }
        if (user.getStatus() == AccountStatus.LOCKED && (user.getLockedUntil() == null || user.getLockedUntil().isAfter(now))) {
            throw new UnauthorizedException(ErrorCode.ACCOUNT_LOCKED, "The account is temporarily locked.");
        }
        user.setStatus(AccountStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        users.saveAndFlush(user);
        audit.record(user.getId(), AuditEventType.ADMIN_LOGIN_SUCCEEDED, "admin_user", user.getId().toString(), Map.of("username", user.getUsername()));
        return issue(user, now, Ids.random());
    }

    public TokenPair refresh(String rawToken, String clientAddress) {
        rateLimiter.checkRefresh(clientAddress);
        Rotation rotation = self.getObject().rotate(rawToken);
        if (rotation.failure() != null) {
            if (rotation.familyId() != null) {
                self.getObject().revokeReusedFamily(rotation.familyId(), rotation.userId(), rotation.tokenId());
            }
            throw rotation.failure();
        }
        return rotation.pair();
    }

    @Transactional
    public Rotation rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw invalidToken();
        }
        AdminRefreshTokenEntity token = refreshTokens.lockByHash(TokenSecrets.sha256(rawToken)).orElseThrow(this::invalidToken);
        Instant now = timeProvider.now();
        if (token.getRevokedAt() != null) {
            return Rotation.reused(token.getFamilyId(), token.getUserId(), token.getId());
        }
        if (!token.getExpiresAt().isAfter(now)) {
            token.setRevokedAt(now);
            refreshTokens.saveAndFlush(token);
            return Rotation.rejected(new UnauthorizedException(ErrorCode.TOKEN_EXPIRED, "The refresh token has expired."));
        }
        AdminUserEntity user = users.findById(token.getUserId()).orElseThrow(this::invalidToken);
        if (user.getStatus() == AccountStatus.DISABLED) {
            throw new UnauthorizedException(ErrorCode.ACCOUNT_DISABLED, "The account is disabled.");
        }
        if (user.getStatus() == AccountStatus.LOCKED && (user.getLockedUntil() == null || user.getLockedUntil().isAfter(now))) {
            throw new UnauthorizedException(ErrorCode.ACCOUNT_LOCKED, "The account is temporarily locked.");
        }
        IssuedRefresh replacement = newRefreshToken(user.getId(), token.getFamilyId(), now);
        token.setRevokedAt(now);
        token.setReplacedBy(replacement.id());
        refreshTokens.saveAndFlush(token);
        return Rotation.success(pair(user, accessTokens.issue(user.getId(), now), replacement));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeReusedFamily(UUID familyId, UUID userId, UUID tokenId) {
        refreshTokens.revokeFamily(familyId, timeProvider.now());
        audit.record(userId, AuditEventType.ADMIN_LOGIN_FAILED, "refresh_token", tokenId.toString(), Map.of("reason", "reused"));
    }

    @Transactional
    public void logout() {
        AuthenticatedAccess access = authorization.requireAccess();
        Instant now = timeProvider.now();
        refreshTokens.revokeAllForUser(access.userId(), now);
        denylist.revoke(access.tokenId(), properties.getAccessTokenTtl());
        audit.record(access.userId(), AuditEventType.ADMIN_LOGOUT, "admin_user", access.userId().toString(), Map.of());
    }

    @Transactional
    public TokenPair changePassword(String currentPassword, String newPassword) {
        PasswordPolicy.check(newPassword);
        AuthenticatedAccess access = authorization.requireAccess();
        AdminUserEntity user = users.lockById(access.userId()).orElseThrow();
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, "The current password is incorrect.");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new ValidationException("Choose a different password.", List.of(new FieldErrorDetail("password", "Choose a different password.")));
        }
        Instant now = timeProvider.now();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(now);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(now);
        users.saveAndFlush(user);
        refreshTokens.revokeAllForUser(user.getId(), now);
        denylist.revoke(access.tokenId(), properties.getAccessTokenTtl());
        audit.record(user.getId(), AuditEventType.ADMIN_PASSWORD_CHANGED, "admin_user", user.getId().toString(), Map.of("username", user.getUsername()));
        return issue(user, now, Ids.random());
    }

    @Transactional(readOnly = true)
    public SessionView session() {
        AuthenticatedAccess access = authorization.requireAccess();
        AdminUserEntity user = users.findById(access.userId()).orElseThrow();
        return new SessionView(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.isMustChangePassword(),
                links.findRoleCodes(user.getId()),
                links.findPermissionCodes(user.getId()));
    }

    public void revokeRefreshTokens(UUID userId) {
        refreshTokens.revokeAllForUser(userId, timeProvider.now());
    }

    private TokenPair issue(AdminUserEntity user, Instant now, UUID familyId) {
        IssuedRefresh refresh = newRefreshToken(user.getId(), familyId, now);
        return pair(user, accessTokens.issue(user.getId(), now), refresh);
    }

    private IssuedRefresh newRefreshToken(UUID userId, UUID familyId, Instant now) {
        String raw = TokenSecrets.randomToken();
        AdminRefreshTokenEntity entity = new AdminRefreshTokenEntity();
        entity.setId(Ids.random());
        entity.setUserId(userId);
        entity.setFamilyId(familyId);
        entity.setTokenHash(TokenSecrets.sha256(raw));
        entity.setExpiresAt(now.plus(properties.getRefreshTokenTtl()));
        entity.setCreatedAt(now);
        refreshTokens.saveAndFlush(entity);
        return new IssuedRefresh(entity.getId(), raw, entity.getExpiresAt());
    }

    private TokenPair pair(AdminUserEntity user, JwtAccessTokens.Issued access, IssuedRefresh refresh) {
        return new TokenPair(
                access.value(),
                access.expiresAt(),
                refresh.rawToken(),
                refresh.expiresAt(),
                user.isMustChangePassword());
    }

    private record IssuedRefresh(UUID id, String rawToken, Instant expiresAt) {
    }

    public record Rotation(TokenPair pair, UnauthorizedException failure, UUID familyId, UUID userId, UUID tokenId) {
        static Rotation success(TokenPair pair) {
            return new Rotation(pair, null, null, null, null);
        }

        static Rotation rejected(UnauthorizedException failure) {
            return new Rotation(null, failure, null, null, null);
        }

        static Rotation reused(UUID familyId, UUID userId, UUID tokenId) {
            return new Rotation(
                    null,
                    new UnauthorizedException(ErrorCode.TOKEN_REUSED, "The refresh token was already used."),
                    familyId,
                    userId,
                    tokenId);
        }
    }

    private static String normalizeUsername(String rawUsername) {
        try {
            return Usernames.normalize(rawUsername);
        } catch (ValidationException exception) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, INVALID_CREDENTIALS);
        }
    }

    private UnauthorizedException invalidToken() {
        return new UnauthorizedException(ErrorCode.TOKEN_INVALID, "The refresh token is invalid.");
    }
}
