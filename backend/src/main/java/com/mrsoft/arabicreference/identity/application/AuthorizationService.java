package com.mrsoft.arabicreference.identity.application;

import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.UnauthorizedException;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
public class AuthorizationService {

    public boolean has(String permission) {
        AuthenticatedAccess access = currentOrNull();
        return access != null && !access.mustChangePassword() && access.permissions().contains(permission);
    }

    public AuthenticatedAccess requireAccess() {
        AuthenticatedAccess access = currentOrNull();
        if (access == null) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "Authentication is required for this operation.");
        }
        return access;
    }

    public void requireGrantable(Collection<String> permissions) {
        AuthenticatedAccess access = requireAccess();
        if (!access.permissions().containsAll(permissions)) {
            throw new ForbiddenOperationException("You cannot grant a permission you do not hold.");
        }
    }

    private static AuthenticatedAccess currentOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccess access)) {
            return null;
        }
        return access;
    }
}
