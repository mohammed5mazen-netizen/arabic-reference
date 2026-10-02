package com.mrsoft.arabicreference.source.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LicensePolicyTest {

    @Test
    void unknownAndRestrictedStayOffThePublicSite() {
        assertThat(LicensePolicy.allowsPublicAttribution(LicenseType.PUBLIC_DOMAIN)).isTrue();
        assertThat(LicensePolicy.allowsPublicAttribution(LicenseType.CC_BY)).isTrue();
        assertThat(LicensePolicy.allowsPublicAttribution(LicenseType.PERMISSION_GRANTED)).isTrue();
        assertThat(LicensePolicy.allowsPublicAttribution(LicenseType.UNKNOWN)).isFalse();
        assertThat(LicensePolicy.allowsPublicAttribution(LicenseType.RESTRICTED)).isFalse();
    }
}
