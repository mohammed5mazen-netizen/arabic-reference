package com.mrsoft.arabicreference.source.domain;

/**
 * Unknown and restricted licenses may be catalogued. They must not be published or cited on the public site.
 */
public final class LicensePolicy {

    private LicensePolicy() {
    }

    public static boolean allowsPublicAttribution(LicenseType license) {
        return switch (license) {
            case PUBLIC_DOMAIN, CC0, CC_BY, CC_BY_SA, PERMISSION_GRANTED -> true;
            case RESTRICTED, UNKNOWN -> false;
        };
    }
}
