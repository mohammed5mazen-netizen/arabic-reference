package com.mrsoft.arabicreference.shared.kernel.security;

import java.util.Set;
import java.util.UUID;

/**
 * Request principal for an editorial staff access token.
 * Permissions are loaded from the database for this request. They are not copied from the token.
 */
public record AuthenticatedAccess(
        UUID userId,
        String tokenId,
        boolean mustChangePassword,
        Set<String> permissions) {

    public AuthenticatedAccess {
        permissions = Set.copyOf(permissions);
    }
}
