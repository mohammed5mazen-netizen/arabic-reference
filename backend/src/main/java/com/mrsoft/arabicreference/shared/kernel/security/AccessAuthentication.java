package com.mrsoft.arabicreference.shared.kernel.security;

import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;

public sealed interface AccessAuthentication
        permits AccessAuthentication.Authenticated, AccessAuthentication.Rejected {

    record Authenticated(AuthenticatedAccess access) implements AccessAuthentication {
    }

    record Rejected(ErrorCode code, String message) implements AccessAuthentication {
    }
}
