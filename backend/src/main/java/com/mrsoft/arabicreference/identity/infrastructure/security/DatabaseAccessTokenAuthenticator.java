package com.mrsoft.arabicreference.identity.infrastructure.security;

import com.mrsoft.arabicreference.identity.domain.AccountStatus;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRepository;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRoleRepository;
import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.security.AccessAuthentication;
import com.mrsoft.arabicreference.shared.kernel.security.AccessTokenAuthenticator;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAccessTokenAuthenticator implements AccessTokenAuthenticator {

    private final JwtAccessTokens tokens;
    private final RedisAccessTokenDenylist denylist;
    private final AdminUserRepository users;
    private final AdminUserRoleRepository roles;
    private final TimeProvider timeProvider;

    public DatabaseAccessTokenAuthenticator(
            JwtAccessTokens tokens,
            RedisAccessTokenDenylist denylist,
            AdminUserRepository users,
            AdminUserRoleRepository roles,
            TimeProvider timeProvider) {
        this.tokens = tokens;
        this.denylist = denylist;
        this.users = users;
        this.roles = roles;
        this.timeProvider = timeProvider;
    }

    @Override
    public AccessAuthentication authenticate(String token) {
        Jwt jwt;
        try {
            jwt = tokens.decode(token);
        } catch (JwtException exception) {
            if (expired(exception)) {
                return rejected(ErrorCode.TOKEN_EXPIRED, "The access token has expired.");
            }
            return rejected(ErrorCode.TOKEN_INVALID, "The access token is invalid.");
        }
        UUID userId;
        UUID tokenId;
        try {
            userId = UUID.fromString(jwt.getSubject());
            tokenId = UUID.fromString(jwt.getId());
        } catch (RuntimeException exception) {
            return rejected(ErrorCode.TOKEN_INVALID, "The access token is invalid.");
        }
        try {
            if (denylist.isRevoked(tokenId.toString())) {
                return rejected(ErrorCode.TOKEN_INVALID, "The access token has been revoked.");
            }
        } catch (RuntimeException exception) {
            return rejected(ErrorCode.SERVICE_UNAVAILABLE, "Authentication is temporarily unavailable.");
        }
        var user = users.findById(userId).orElse(null);
        if (user == null) {
            return rejected(ErrorCode.TOKEN_INVALID, "The access token is invalid.");
        }
        if (user.getStatus() == AccountStatus.DISABLED) {
            return rejected(ErrorCode.ACCOUNT_DISABLED, "The account is disabled.");
        }
        if (user.getStatus() == AccountStatus.LOCKED
                && (user.getLockedUntil() == null || user.getLockedUntil().isAfter(timeProvider.now()))) {
            return rejected(ErrorCode.ACCOUNT_LOCKED, "The account is temporarily locked.");
        }
        var permissions = Set.copyOf(roles.findPermissionCodes(userId));
        return new AccessAuthentication.Authenticated(
                new AuthenticatedAccess(userId, tokenId.toString(), user.isMustChangePassword(), permissions));
    }

    private static AccessAuthentication rejected(ErrorCode code, String message) {
        return new AccessAuthentication.Rejected(code, message);
    }

    private static boolean expired(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage() == null ? "" : current.getMessage().toLowerCase(Locale.ROOT);
            if (message.contains("expired")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
