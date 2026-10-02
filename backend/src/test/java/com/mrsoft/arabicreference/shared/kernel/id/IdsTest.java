package com.mrsoft.arabicreference.shared.kernel.id;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdsTest {

    @Test
    void generatesRandomUuidVersion4() {
        UUID first = Ids.random();
        UUID second = Ids.random();

        assertThat(first.version()).isEqualTo(4);
        assertThat(second.version()).isEqualTo(4);
        assertThat(first).isNotEqualTo(second);
    }
}
