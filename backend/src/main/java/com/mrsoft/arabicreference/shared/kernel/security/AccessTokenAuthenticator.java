package com.mrsoft.arabicreference.shared.kernel.security;

/**
 * Validates an admin access token and loads the current account state.
 * The shared security filter depends on this port so it does not reference the identity module.
 */
public interface AccessTokenAuthenticator {

    AccessAuthentication authenticate(String token);
}
