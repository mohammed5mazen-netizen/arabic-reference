package com.mrsoft.arabicreference.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

    @Test
    void acceptsALongMixedPassword() {
        PasswordPolicy.check("Owner-Pass-123!");
    }

    @Test
    void rejectsAShortPassword() {
        assertThatThrownBy(() -> PasswordPolicy.check("Short-1!"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void usernameNormalizationIsCaseInsensitive() {
        assertThat(Usernames.normalize("Owner.Admin")).isEqualTo("owner.admin");
    }

    @Test
    void emailNormalizationIsCaseInsensitive() {
        assertThat(Emails.normalize("Owner@Arabic-Reference.TEST")).isEqualTo("owner@arabic-reference.test");
    }

    @Test
    void temporaryPasswordsMeetThePolicy() {
        PasswordPolicy.check(TemporaryPasswords.generate());
    }

    @Test
    void bootstrapIsIncompleteWhenAnyValueIsMissing() {
        var bootstrap = new com.mrsoft.arabicreference.identity.application.AdminSecurityProperties.Bootstrap();
        assertThat(bootstrap.complete()).isFalse();
        bootstrap.setUsername("owner");
        bootstrap.setEmail("owner@arabic-reference.test");
        bootstrap.setDisplayName("Platform Owner");
        assertThat(bootstrap.complete()).isFalse();
    }
}
